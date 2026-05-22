package be.ap.backend.controller.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import be.ap.backend.entity.Location;
import be.ap.backend.repository.LocationRepository;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("auth")
public class AuthController {

    @Autowired
    private LocationRepository locationRepository;

    @GetMapping("me")
    public ResponseEntity<?> me(HttpSession session) {
        final String username = (String) session.getAttribute("username");
        final String role = (String) session.getAttribute("role");
        final String school = (String) session.getAttribute("school");

        List<Location> locations = locationRepository.getLocationBySchoolId(Long.parseLong(school));

        final Object userIdRaw = session.getAttribute("userId");
        final Object schoolRaw = session.getAttribute("school"); // voeg toe

        final Long userId = (userIdRaw != null) ? Long.valueOf(userIdRaw.toString()) : null;
        final Long schoolId = (schoolRaw != null) ? Long.valueOf(schoolRaw.toString()) : null; // voeg toe

        return ResponseEntity.ok(Map.of(
                "username", username,
                "location", locations,
                "role", role,
                "userId", userId != null ? userId : 0,
                "schoolId", schoolId != null ? schoolId : 0)); // voeg toe
    }

    @GetMapping("me/id")
    public ResponseEntity<?> meId(HttpSession session) {
        final Object raw = session.getAttribute("userId");
        final Long userId = (raw != null) ? Long.valueOf(raw.toString()) : null;
        return ResponseEntity.ok(Map.of("userId", userId));
    }
}
