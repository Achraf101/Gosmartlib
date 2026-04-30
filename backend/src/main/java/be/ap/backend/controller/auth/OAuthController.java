package be.ap.backend.controller.auth;

import be.ap.backend.repository.CampusRepository;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;
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

import be.ap.backend.dto.GroupDTO;
import be.ap.backend.dto.GroupsResponseDto;
import be.ap.backend.entity.Campus;
import be.ap.backend.entity.School;
import be.ap.backend.entity.User;
import be.ap.backend.entity.UserRole;
import be.ap.backend.repository.SchoolRepository;
import be.ap.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
public class OAuthController {
    private final CampusRepository campusRepository;

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;

    @Value("${app.smartschool.client-id}")
    private String clientId;

    @Value("${app.smartschool.client-secret}")
    private String clientSecret;

    @Value("${app.smartschool.callback}")
    private String callback;

    public OAuthController(UserRepository userRepository, SchoolRepository schoolRepository,
            CampusRepository campusRepository) {
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.campusRepository = campusRepository;
    }

    @GetMapping("oauth") // smartschool oauth
    public ResponseEntity<?> getMethodName(@RequestParam String code, @RequestParam String originplatform,
            HttpServletRequest httpRequest)
            throws Exception {
        AuthorizationCode authCode = new AuthorizationCode(code);

        URI tokenEndpoint = new URI("https://" + originplatform + ".smartschool.be/OAuth/index/token");
        String userInfoEndpoint = "https://" + originplatform + ".smartschool.be/Api/V1/userinfo";
        String userGroupEndpoint = "https://" + originplatform + ".smartschool.be/Api/V1/groupinfo";

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

        // get userinfo (smartschool userID)
        UserInfo userInfo = getUserInfo(tokens, userInfoEndpoint);

        // check if user already exists (create if not)
        Optional<User> optUser = userRepository.findBySsId(userInfo.userId);

        // user exists create session
        if (!optUser.isEmpty()) {
            User user = optUser.get();
            // create session with extra attributes
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
            session.setAttribute("campus", user.getCampus().getId());
            session.setAttribute("school", user.getSchool().getId());
            session.setAttribute("username", user.getSsName());

            return ResponseEntity.status(HttpStatus.FOUND).header("Location",
                    "/").build();
        }

        // register the new user
        // get user role from smartschool
        List<String> groups = getUserGroups(tokens, userGroupEndpoint);
        groups = groups.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        User user = new User();
        if (groups.contains("leerkrachten") || groups.contains("leerkracht")) {
            user.setRole(UserRole.LEERKRACHT);
        } else if (groups.contains("leerlingen") || groups.contains("leerling") || groups.contains("studenten")
                || groups.contains("student")) {
            user.setRole(UserRole.STUDENT);
        } else {
            // gets lowest role as default if nothing matches
            user.setRole(UserRole.STUDENT);
        }

        School school = schoolRepository.findBySsSubdomain(originplatform).orElse(null);
        Campus campus = campusRepository.findBySchool(school).getFirst();

        if (campus.equals(null))
            return ResponseEntity.status(500).body("Geen campus gevonden");

        user.setSsName(userInfo.fullName);
        user.setSchool(school);
        user.setCampus(campus);
        user.setSsRefresh(tokens.getRefreshToken().getValue());
        user.setSsAccess(tokens.getAccessToken().getValue());
        user.setSsId(userInfo.userId);
        user.setSsName(userInfo.fullName);

        userRepository.save(user);

        // create session
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
        session.setAttribute("campus", campus.getId());
        session.setAttribute("school", school.getId());
        session.setAttribute("username", user.getSsName());

        System.out.println(session.toString());

        return ResponseEntity.status(HttpStatus.FOUND).header("Location", "/").build();
    }

    private UserInfo getUserInfo(Tokens tokens, String userInfoUrl) {

        RestTemplate rest = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(tokens.getAccessToken().getValue());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, String>> res = rest.exchange(
                userInfoUrl,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Map<String, String>>() {
                });

        Map<String, String> user = res.getBody();

        UserInfo ui = new UserInfo();
        ui.userId = (String) user.get("userID");
        ui.fullName = (String) user.get("fullname");

        return ui;
    }

    private List<String> getUserGroups(Tokens tokens, String userGroupUrl) {
        RestTemplate rest = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokens.getAccessToken().getValue());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<GroupsResponseDto> res = rest.exchange(
                userGroupUrl,
                HttpMethod.GET,
                entity,
                GroupsResponseDto.class);

        GroupsResponseDto data = res.getBody();

        List<String> groups = data.getParentGroups()
                .stream()
                .map(GroupDTO::getName)
                .toList();

        return groups;
    }

    private class UserInfo {
        public String userId;
        public String fullName;
    }
}
