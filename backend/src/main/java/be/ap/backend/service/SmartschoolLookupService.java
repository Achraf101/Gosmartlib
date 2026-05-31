package be.ap.backend.service;

import be.ap.backend.entity.User;
import be.ap.backend.dto.TeacherDTO;
import be.ap.backend.entity.School;
import be.ap.backend.entity.UserRole;
import be.ap.backend.repository.SchoolRepository;
import be.ap.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartschoolLookupService {

    private final SmartschoolTokenService tokenService;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;

    @SuppressWarnings("unchecked")
    public Map<String, Object> getUser(Long schoolId, String oneRosterId, Set<UserRole> roles) {
        School school = getSchool(schoolId);
        String endpoint = roles.contains(UserRole.STUDENT) ? "students" : "teachers";
        String url = "https://" + school.getSsSubdomain() + ".smartschool.be/ims/oneroster/v1p1/" + endpoint + "/"
                + oneRosterId;
        Map<String, Object> response = get(school, url);
        if (response == null)
            return null;
        return (Map<String, Object>) response.getOrDefault("user", response);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getClassroom(Long schoolId, String ssId) {
        School school = getSchool(schoolId);
        String url = "https://" + school.getSsSubdomain() + ".smartschool.be/ims/oneroster/v1p1/classes/" + ssId;
        Map<String, Object> response = get(school, url);
        if (response == null)
            return null;
        return (Map<String, Object>) response.get("class");
    }

    @SuppressWarnings("unchecked")
    public List<TeacherDTO> getAllTeachersForSchool(Long schoolId) {
        School school = getSchool(schoolId);
        String url = "https://" + school.getSsSubdomain() + ".smartschool.be/ims/oneroster/v1p1/schools/"
                + school.getSsId() + "/teachers";
        Map<String, Object> response = get(school, url);
        if (response == null)
            return List.of();
        List<Map<String, Object>> users = (List<Map<String, Object>>) response.get("users");
        return users.stream()
                .map(user -> {
                    String sourcedId = (String) user.get("sourcedId");
                    User dbUser = userRepository.findByOneRosterId(sourcedId).orElse(null);
                    if (dbUser == null)
                        return null;
                    return new TeacherDTO(
                            dbUser.getId(),
                            (String) user.get("givenName"),
                            (String) user.get("familyName"));
                })
                .filter(t -> t != null)
                .collect(Collectors.toList());
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

    private School getSchool(Long schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new EntityNotFoundException("School niet gevonden: " + schoolId));
    }
}