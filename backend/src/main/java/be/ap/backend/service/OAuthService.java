package be.ap.backend.service;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

import be.ap.backend.dto.GroupDTO;
import be.ap.backend.dto.GroupsResponseDto;
import be.ap.backend.entity.Location;
import be.ap.backend.entity.School;
import be.ap.backend.entity.User;
import be.ap.backend.entity.UserRole;
import be.ap.backend.repository.LocationRepository;
import be.ap.backend.repository.SchoolRepository;
import be.ap.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final LocationRepository locationRepository;

    @Value("${app.smartschool.client-id}")
    private String clientId;

    @Value("${app.smartschool.client-secret}")
    private String clientSecret;

    @Value("${app.smartschool.callback}")
    private String callback;

    public void handleCallback(String code, String originplatform, HttpServletRequest httpRequest) throws Exception {
        AuthorizationCode authCode = new AuthorizationCode(code);

        URI tokenEndpoint = new URI("https://" + originplatform + ".smartschool.be/OAuth/index/token");
        String userInfoEndpoint = "https://" + originplatform + ".smartschool.be/Api/V1/userinfo";
        String userGroupEndpoint = "https://" + originplatform + ".smartschool.be/Api/V1/groupinfo";

        TokenRequest request = new TokenRequest(
                tokenEndpoint,
                new ClientSecretBasic(new ClientID(clientId), new Secret(clientSecret)),
                new AuthorizationCodeGrant(authCode, new URI(callback)));

        HTTPResponse httpResponse = request.toHTTPRequest().send();
        TokenResponse response = TokenResponse.parse(httpResponse);

        if (!response.indicatesSuccess()) {
            throw new IllegalArgumentException("Probleem bij het inloggen met SmartSchool.");
        }

        Tokens tokens = response.toSuccessResponse().getTokens();
        UserInfo userInfo = getUserInfo(tokens, userInfoEndpoint);

        Optional<User> optUser = userRepository.findBySsId(userInfo.userId);

        User user;
        if (optUser.isPresent()) {
            user = optUser.get();
        } else {
            List<String> groups = getUserGroups(tokens, userGroupEndpoint).stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());

            user = new User();
            if (groups.contains("leerkrachten") || groups.contains("leerkracht")) {
                user.setRole(UserRole.LEERKRACHT);
            } else {
                user.setRole(UserRole.STUDENT);
            }

            School school = schoolRepository.findBySsSubdomain(originplatform)
                    .orElseThrow(() -> new EntityNotFoundException("School niet gevonden voor: " + originplatform));
            Location location = locationRepository.findBySchool(school).stream().findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Geen locatie gevonden voor school: " + originplatform));

            user.setSsName(userInfo.fullName);
            user.setSchool(school);
            user.setLocation(location);
            user.setSsRefresh(tokens.getRefreshToken().getValue());
            user.setSsAccess(tokens.getAccessToken().getValue());
            user.setSsId(userInfo.userId);

            userRepository.save(user);
        }

        createSession(httpRequest, user);
    }

    private void createSession(HttpServletRequest httpRequest, User user) {
        HttpSession session = httpRequest.getSession(true);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getId(), null, user.getAuthorities());

        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(auth);

        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
        session.setAttribute("userId", user.getId());
        session.setAttribute("role", user.getRole().name());
        session.setAttribute("location", user.getLocation().getId());
        session.setAttribute("school", user.getSchool().getId());
        session.setAttribute("username", user.getSsName());
    }

    private UserInfo getUserInfo(Tokens tokens, String userInfoUrl) {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokens.getAccessToken().getValue());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, String>> res = rest.exchange(userInfoUrl, HttpMethod.GET, entity,
                new ParameterizedTypeReference<Map<String, String>>() {});

        Map<String, String> user = res.getBody();
        UserInfo ui = new UserInfo();
        ui.userId = user.get("userID");
        ui.fullName = user.get("fullname");
        return ui;
    }

    private List<String> getUserGroups(Tokens tokens, String userGroupUrl) {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokens.getAccessToken().getValue());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<GroupsResponseDto> res = rest.exchange(userGroupUrl, HttpMethod.GET, entity,
                GroupsResponseDto.class);

        return res.getBody().getParentGroups().stream()
                .map(GroupDTO::getName)
                .toList();
    }

    public static class UserInfo {
        public String userId;
        public String fullName;
    }
}