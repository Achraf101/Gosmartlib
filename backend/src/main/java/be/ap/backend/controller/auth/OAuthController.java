package be.ap.backend.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    @GetMapping("oauth")
    public ResponseEntity<?> handleOAuth(@RequestParam String code, @RequestParam String originplatform,
            HttpServletRequest httpRequest) throws Exception {
        return oAuthService.handleCallback(code, originplatform, httpRequest);
    }
}