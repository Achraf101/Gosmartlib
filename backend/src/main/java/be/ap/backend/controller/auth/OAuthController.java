package be.ap.backend.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Handles OAuth callback requests from external authentication providers.
 *
 * <p>
 * Delegates processing of authorization codes to the OAuth service layer.
 * </p>
 */
@RestController
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    /**
     * Processes the OAuth authorization callback.
     *
     * <p>
     * Receives an authorization code and forwards it to the OAuth service for
     * validation
     * and session/token creation.
     * </p>
     *
     * @param code           authorization code returned by the OAuth provider
     * @param originplatform identifier of the originating platform
     * @param httpRequest    current HTTP request context
     * @return response from the OAuth service after processing the login flow
     * @throws Exception if OAuth processing fails
     */
    @GetMapping("oauth")
    public ResponseEntity<?> handleOAuth(@RequestParam String code, @RequestParam String originplatform,
            HttpServletRequest httpRequest) throws Exception {
        return oAuthService.handleCallback(code, originplatform, httpRequest);
    }
}