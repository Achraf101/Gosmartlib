package be.ap.backend.controller.auth;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Optional;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
// import org.hibernate.mapping.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;

import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.*;
import com.nimbusds.oauth2.sdk.token.Tokens;

import be.ap.backend.entity.School;
import be.ap.backend.entity.User;
import be.ap.backend.repository.SchoolRepository;
import be.ap.backend.repository.UserRepository;
import be.ap.backend.service.SmartschoolLookupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OAuthController {
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final SmartschoolLookupService lookupService;

    @Value("${app.smartschool.client-id}")
    private String clientId;

    @Value("${app.smartschool.client-secret}")
    private String clientSecret;

    @Value("${app.smartschool.callback}")
    private String callback;

    @GetMapping("oauth") // smartschool oauth
    public ResponseEntity<?> getMethodName(@RequestParam String code, @RequestParam String originplatform,
            HttpServletRequest httpRequest)
            throws Exception {
        AuthorizationCode authCode = new AuthorizationCode(code);

        URI tokenEndpoint = new URI("https://" + originplatform + ".smartschool.be/OAuth/index/token");

        TokenRequest request = new TokenRequest(
                tokenEndpoint,
                new ClientSecretBasic(
                        new ClientID(this.clientId), // todo read from variable
                        new Secret(this.clientSecret)),
                new AuthorizationCodeGrant(
                        authCode,
                        new URI(this.callback)));

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

        // check if user already exists (create if not)
        Optional<User> optUser = userRepository.findBySsId(ssId);

        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Gebruiker nog niet gesynchroniseerd. Neem contact op met je administrator.");
        }

        User user = optUser.get();

        School school = schoolRepository.findBySsSubdomain(originplatform)
                .orElse(null);
        if (school == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("School niet gevonden.");
        }

        String roleEndpoint = user.getRole().name().equals("STUDENT") ? "student" : "teacher";
        Map<String, Object> orUser = lookupService.getUser(school, user.getOneRosterId(), roleEndpoint);

        String firstName = orUser != null ? (String) orUser.get("givenName") : "";
        String lastName = orUser != null ? (String) orUser.get("familyName") : "";
        String email = orUser != null ? (String) orUser.get("email") : "";
        Long locationId = school.getLocations().isEmpty() ? null : school.getLocations().get(0).getId();

        HttpSession session = httpRequest.getSession(true);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getId(),
                null,
                user.getAuthorities());

        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(auth);

        // 👇 manually save security context to session
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                securityContext);

        session.setAttribute("userId", user.getId());
        session.setAttribute("role", user.getRole().name());
        session.setAttribute("school", school.getId());
        session.setAttribute("location", locationId);
        session.setAttribute("firstName", firstName);
        session.setAttribute("lastName", lastName);
        session.setAttribute("email", email);

        return ResponseEntity.status(HttpStatus.FOUND).header("Location",
                "/").build();
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