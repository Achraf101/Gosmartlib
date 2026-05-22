package be.ap.backend.service;

import be.ap.backend.entity.School;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartschoolLookupService {

    private final SmartschoolTokenService tokenService;
    private final RestTemplate restTemplate;

    @SuppressWarnings("unchecked")
    public Map<String, Object> getUser(School school, String oneRosterId, String role) {
        String endpoint = "student".equals(role) ? "students" : "teachers";
        String url = "https://" + school.getSsSubdomain() + ".smartschool.be/ims/oneroster/v1p1/" + endpoint + "/"
                + oneRosterId;
        Map<String, Object> response = get(school, url);
        if (response == null)
            return null;
        return (Map<String, Object>) response.getOrDefault("user", response);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getClassroom(School school, String ssId) {
        String url = "https://" + school.getSsSubdomain() + ".smartschool.be/ims/oneroster/v1p1/classes/" + ssId;
        Map<String, Object> response = get(school, url);
        if (response == null)
            return null;
        return (Map<String, Object>) response.get("class");
    }

    // @SuppressWarnings("unchecked")
    // public List<Map<String, Object>> getClassesForUser(School school, String
    // ssId) {
    // String url = "https://" + school.getSsSubdomain() +
    // ".smartschool.be/ims/oneroster/v1p1/users/" + ssId
    // + "/classes";
    // Map<String, Object> response = get(school, url);
    // if (response == null)
    // return List.of();
    // return (List<Map<String, Object>>) response.get("classes");
    // }

    // @SuppressWarnings("unchecked")
    // public List<Map<String, Object>> getEnrollmentsForUser(School school, String
    // ssId) {
    // String url = "https://" + school.getSsSubdomain()
    // + ".smartschool.be/ims/oneroster/v1p1/enrollments?filter=user.sourcedId='" +
    // ssId + "'";
    // Map<String, Object> response = get(school, url);
    // if (response == null)
    // return List.of();
    // return (List<Map<String, Object>>) response.get("enrollments");
    // }

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
}