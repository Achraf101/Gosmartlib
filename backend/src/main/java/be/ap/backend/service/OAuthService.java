package be.ap.backend.service;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.Tokens;

import be.ap.backend.entity.School;
import be.ap.backend.entity.User;
import be.ap.backend.entity.UserRole;
import be.ap.backend.repository.SchoolRepository;
import be.ap.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final SmartschoolLookupService lookupService;

    @Value("${app.smartschool.client-id}")
    private String clientId;

    @Value("${app.smartschool.client-secret}")
    private String clientSecret;

    @Value("${app.smartschool.callback}")
    private String callback;

    public ResponseEntity<?> handleCallback(String code, String originplatform, HttpServletRequest httpRequest)
            throws Exception {
        AuthorizationCode authCode = new AuthorizationCode(code);

        URI tokenEndpoint = new URI("https://" + originplatform + ".smartschool.be/OAuth/index/token");

        TokenRequest request = new TokenRequest(
                tokenEndpoint,
                new ClientSecretBasic(new ClientID(clientId), new Secret(clientSecret)),
                new AuthorizationCodeGrant(authCode, new URI(callback)));

        HTTPResponse httpResponse = request.toHTTPRequest().send();
        TokenResponse response = TokenResponse.parse(httpResponse);

        if (!response.indicatesSuccess()) {
            return ResponseEntity.badRequest().body("Probleem bij het inloggen met SmartSchool.");
        }

        Tokens tokens = response.toSuccessResponse().getTokens();

        String ssId = getSsId(tokens, "https://" + originplatform + ".smartschool.be/Api/V1/userinfo");
        if (ssId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kon gebruiker niet ophalen van SmartSchool.");
        }

        Optional<User> optUser = userRepository.findBySsId(ssId);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Gebruiker nog niet gesynchroniseerd. Neem contact op met je administrator.");
        }

        User user = optUser.get();

        // save refresh token for notifications
        System.out.println("#################refresh##############");
        user.setSsRefresh(tokens.getRefreshToken().toString());
        userRepository.save(user);

        School school = schoolRepository.findBySsSubdomain(originplatform).orElse(null);
        if (school == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("School niet gevonden.");
        }

        Map<String, Object> orUser = lookupService.getUser(school, user.getOneRosterId(), user.getRoles());

        String firstName = orUser != null ? (String) orUser.get("givenName") : "";
        String lastName = orUser != null ? (String) orUser.get("familyName") : "";
        String email = orUser != null ? (String) orUser.get("email") : "";
        Long locationId = school.getLocations().isEmpty() ? null : school.getLocations().get(0).getId();

        createSession(httpRequest, user, school, locationId, firstName, lastName, email);

        return ResponseEntity.status(HttpStatus.FOUND).header("Location", "/").build();
    }

    private void createSession(HttpServletRequest httpRequest, User user, School school, Long locationId,
            String firstName, String lastName, String email) {
        HttpSession session = httpRequest.getSession(true);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getId(), null, user.getAuthorities());

        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(auth);

        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
        session.setAttribute("userId", user.getId());
        session.setAttribute("roles", user.getRoles().stream()
                .map(UserRole::name)
                .collect(Collectors.toSet()));
        session.setAttribute("school", school.getId());
        session.setAttribute("location", locationId);
        session.setAttribute("firstName", firstName);
        session.setAttribute("lastName", lastName);
        session.setAttribute("email", email);
    }

    private String getSsId(Tokens tokens, String userInfoUrl) {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokens.getAccessToken().getValue());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, String>> res = rest.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Map<String, String>>() {
                    });
            Map<String, String> body = res.getBody();
            return body != null ? body.get("userID") : null;
        } catch (Exception e) {
            return null;
        }
    }
}