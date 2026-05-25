package be.ap.backend.service;

import be.ap.backend.entity.Classroom;
import be.ap.backend.entity.Enrollment;
import be.ap.backend.entity.School;
import be.ap.backend.entity.User;
import be.ap.backend.entity.UserRole;
import be.ap.backend.repository.ClassroomRepository;
import be.ap.backend.repository.EnrollmentRepository;
import be.ap.backend.repository.SchoolRepository;
import be.ap.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartschoolSyncService {

    @Value("${app.smartschool.oneroster.scope}")
    private String scope;

    private final SmartschoolTokenService tokenService;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final RestTemplate restTemplate;
    private final EnrollmentRepository enrollmentRepository;
    private final SchoolRepository schoolRepository;

    private static final int PAGE_SIZE = 100;

    public void syncSchool(School school) {
        log.info("Starting sync for school: {}", school.getSsSubdomain());
        try {
            String baseUrl = "https://" + school.getSsSubdomain() + ".smartschool.be/ims/oneroster/v1p1";
            resolveAndStoreSsId(school, baseUrl);
            syncUsers(school, baseUrl);
            syncClassrooms(school, baseUrl);
            syncEnrollments(school, baseUrl);
            log.info("Sync completed for school: {}", school.getSsSubdomain());
        } catch (Exception e) {
            log.error("Sync failed for school: {}", school.getSsSubdomain(), e);
            throw new RuntimeException("Sync failed for school: " + school.getSsSubdomain(), e);
        }
    }

    private void syncUsers(School school, String baseUrl) {
        syncUsersByRole(school, baseUrl, "students", UserRole.STUDENT);
        syncUsersByRole(school, baseUrl, "teachers", UserRole.LEERKRACHT);
    }

    @SuppressWarnings("unchecked")
    private void syncUsersByRole(School school, String baseUrl, String endpoint, UserRole role) {
        int offset = 0;
        int synced = 0;

        while (true) {
            String url = UriComponentsBuilder.fromUriString(baseUrl + "/" + endpoint)
                    .queryParam("filter", "status='active'")
                    .queryParam("offset", offset)
                    .queryParam("limit", PAGE_SIZE)
                    .toUriString();

            Map<String, Object> response = get(school, url);
            if (response == null)
                break;

            List<Map<String, Object>> users = (List<Map<String, Object>>) response.get("users");
            if (users == null || users.isEmpty())
                break;

            for (Map<String, Object> u : users) {
                String oneRosterId = (String) u.get("sourcedId");
                if (oneRosterId == null)
                    continue;

                Map<String, Object> metadata = (Map<String, Object>) u.get("metadata");
                String legacyId = metadata != null ? (String) metadata.get("smsc.legacyIdentifier") : null;

                User user = legacyId != null
                        ? userRepository.findBySsId(legacyId).orElse(new User())
                        : new User();
                user.setOneRosterId(oneRosterId);
                user.setSsId(legacyId);
                user.getRoles().add(role);
                user.setSchool(school);
                userRepository.save(user);
                synced++;
            }

            if (users.size() < PAGE_SIZE)
                break;
            offset += PAGE_SIZE;
        }

        log.info("Synced {} {}s for school {}", synced, role.name().toLowerCase(), school.getSsSubdomain());
    }

    @SuppressWarnings("unchecked")
    private void syncClassrooms(School school, String baseUrl) {
        int offset = 0;
        int synced = 0;

        while (true) {
            String url = UriComponentsBuilder.fromUriString(baseUrl + "/classes")
                    .queryParam("filter", "status='active'")
                    .queryParam("offset", offset)
                    .queryParam("limit", PAGE_SIZE)
                    .toUriString();

            Map<String, Object> response = get(school, url);
            if (response == null)
                break;

            List<Map<String, Object>> classes = (List<Map<String, Object>>) response.get("classes");
            if (classes == null || classes.isEmpty())
                break;

            for (Map<String, Object> c : classes) {
                String oneRosterId = (String) c.get("sourcedId");
                if (oneRosterId == null)
                    continue;

                Classroom classroom = classroomRepository.findBySsId(oneRosterId).orElse(new Classroom());
                classroom.setSsId(oneRosterId);
                classroom.setSchool(school);
                classroomRepository.save(classroom);
                synced++;
            }

            if (classes.size() < PAGE_SIZE)
                break;
            offset += PAGE_SIZE;
        }

        log.info("Synced {} classrooms for school {}", synced, school.getSsSubdomain());
    }

    @SuppressWarnings("unchecked")
    private void syncEnrollments(School school, String baseUrl) {
        int offset = 0;
        int synced = 0;

        while (true) {
            String url = UriComponentsBuilder.fromUriString(baseUrl + "/enrollments")
                    .queryParam("filter", "status='active'")
                    .queryParam("offset", offset)
                    .queryParam("limit", PAGE_SIZE)
                    .toUriString();

            Map<String, Object> response = get(school, url);
            if (response == null)
                break;

            List<Map<String, Object>> enrollments = (List<Map<String, Object>>) response.get("enrollments");
            if (enrollments == null || enrollments.isEmpty())
                break;

            for (Map<String, Object> e : enrollments) {
                String oneRosterId = (String) e.get("sourcedId");
                if (oneRosterId == null)
                    continue;

                // Resolve user via oneRosterId
                Map<String, Object> userRef = (Map<String, Object>) e.get("user");
                String userOneRosterId = userRef != null ? (String) userRef.get("sourcedId") : null;
                User user = userOneRosterId != null ? userRepository.findByOneRosterId(userOneRosterId).orElse(null)
                        : null;

                // Resolve classroom
                Map<String, Object> classRef = (Map<String, Object>) e.get("class");
                String classSsId = classRef != null ? (String) classRef.get("sourcedId") : null;
                Classroom classroom = classSsId != null ? classroomRepository.findBySsId(classSsId).orElse(null) : null;

                // Resolve role
                String roleStr = (String) e.get("role");
                UserRole role = null;
                if ("student".equals(roleStr))
                    role = UserRole.STUDENT;
                else if ("teacher".equals(roleStr))
                    role = UserRole.LEERKRACHT;

                Enrollment enrollment = enrollmentRepository.findByOneRosterId(oneRosterId).orElse(new Enrollment());
                enrollment.setOneRosterId(oneRosterId);
                enrollment.setUser(user);
                enrollment.setClassroom(classroom);
                enrollment.setRole(role);
                enrollment.setSchool(school);
                enrollmentRepository.save(enrollment);
                synced++;
            }

            if (enrollments.size() < PAGE_SIZE)
                break;
            offset += PAGE_SIZE;
        }

        log.info("Synced {} enrollments for school {}", synced, school.getSsSubdomain());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> get(School school, String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenService.getAccessToken(school));
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            return (Map<String, Object>) response.getBody();
        } catch (Exception e) {
            log.error("GET {} failed: {}", url, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void resolveAndStoreSsId(School school, String baseUrl) {
        if (school.getSsId() != null)
            return;
        Map<String, Object> response = get(school, baseUrl + "/schools");
        if (response == null)
            return;

        List<Map<String, Object>> orgs = (List<Map<String, Object>>) response.get("orgs");
        if (orgs == null || orgs.isEmpty())
            return;

        String sourcedId = (String) orgs.get(0).get("sourcedId");
        if (sourcedId != null) {
            school.setSsId(sourcedId);
            schoolRepository.save(school);
            log.info("Resolved ssId {} for school {}", sourcedId, school.getSsSubdomain());
        }
    }
}