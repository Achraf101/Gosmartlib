package be.ap.backend.service;

import be.ap.backend.entity.School;
import be.ap.backend.model.OneRosterCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartschoolTokenService {

    @Value("${app.smartschool.oneroster.scope}")
    private String scope;

    private final RestTemplate restTemplate;
    private final SchoolService schoolService;

    // Cache per school-ID
    private final ConcurrentHashMap<Long, String> tokenCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Instant> expiryCache = new ConcurrentHashMap<>();

    public String getAccessToken(School school) {
        log.info("############################getAccessToken called for school={}", school.getSsSubdomain());
        Long schoolId = school.getId();

        if (tokenCache.containsKey(schoolId) &&
                Instant.now().isBefore(expiryCache.get(schoolId).minusSeconds(60))) {
            return tokenCache.get(schoolId);
        }

        return fetchNewToken(school);
    }

    @SuppressWarnings("unchecked")
    private String fetchNewToken(School school) {
        String tokenUrl = "https://" + school.getSsSubdomain() + ".smartschool.be/ims/oneroster/token";

        OneRosterCredentials credentials = schoolService.getCredentials(school.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", credentials.getClientId());
        body.add("client_secret", credentials.getClientSecret());
        body.add("scope", scope);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<?> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to obtain token for school: " + school.getSsSubdomain());
        }

        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();

        String token = (String) responseBody.get("access_token");
        int expiresIn = (int) responseBody.get("expires_in");

        tokenCache.put(school.getId(), token);
        expiryCache.put(school.getId(), Instant.now().plusSeconds(expiresIn));

        log.info("Token obtained for school {}", school.getSsSubdomain());
        log.debug("####################################Token request for school={} clientId={} secret-length={}",
                school.getSsSubdomain(),
                credentials.getClientId(),
                credentials.getClientSecret() != null ? credentials.getClientSecret().length() : "null");
        return token;
    }
}