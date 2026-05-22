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
        final Object locationRaw = session.getAttribute("location");
        final Object userIdRaw = session.getAttribute("userId");
        final Object schoolRaw = session.getAttribute("school"); // voeg toe

        final Long locationId = (locationRaw != null && !locationRaw.toString().isBlank())
                ? Long.valueOf(locationRaw.toString())
                : null;
        final Long userId = (userIdRaw != null) ? Long.valueOf(userIdRaw.toString()) : null;
        final Long schoolId = (schoolRaw != null) ? Long.valueOf(schoolRaw.toString()) : null; 

        return ResponseEntity.ok(Map.of(
                "username", username,
                "role", role,
                "locationId", locationId != null ? locationId : 0,
                "userId", userId != null ? userId : 0,
                "schoolId", schoolId != null ? schoolId : 0)); 
    }

    @GetMapping("me/id")
    public ResponseEntity<?> meId(HttpSession session) {
        final Object raw = session.getAttribute("userId");
        final Long userId = (raw != null) ? Long.valueOf(raw.toString()) : null;
        return ResponseEntity.ok(Map.of("userId", userId));
    }
}
