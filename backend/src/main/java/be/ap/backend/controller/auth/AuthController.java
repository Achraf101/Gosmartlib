package be.ap.backend.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.Map;

@RestController
@RequestMapping("auth")
public class AuthController {

    @GetMapping("me")
    public ResponseEntity<?> me(HttpSession session) {

        final String username = (String) session.getAttribute("username");
        final String role = (String) session.getAttribute("role");
        final Object campus = session.getAttribute("campus");
        return ResponseEntity.ok(Map.of("username", username, "role", role, "campusId", campus != null ? campus : 0));
    }

    @GetMapping("me/id")
    public ResponseEntity<?> meId(HttpSession session) {
        final Object raw = session.getAttribute("userId");
        final Long userId = (raw != null) ? Long.valueOf(raw.toString()) : null;
        return ResponseEntity.ok(Map.of("userId", userId));
    }
}
