package be.ap.backend.controller.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import be.ap.backend.entity.Campus;
import be.ap.backend.repository.CampusRepository;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("auth")
public class AuthController {

    @Autowired
    private CampusRepository campusRepository;

    @GetMapping("me")
    public ResponseEntity<?> me(HttpSession session) {
        final String username = (String) session.getAttribute("username");
        final String role = (String) session.getAttribute("role");
        final String school = (String) session.getAttribute("school");

        List<Campus> campuses = campusRepository.getCampusBySchoolId(Long.parseLong(school));

        return ResponseEntity.ok(Map.of("username", username, "role", role, "campus", campuses));
    }

    @GetMapping("me/id")
    public ResponseEntity<?> meId(HttpSession session) {
        final Object raw = session.getAttribute("userId");
        final Long userId = (raw != null) ? Long.valueOf(raw.toString()) : null;
        return ResponseEntity.ok(Map.of("userId", userId));
    }
}
