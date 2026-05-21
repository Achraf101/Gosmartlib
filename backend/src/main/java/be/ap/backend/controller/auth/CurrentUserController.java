package be.ap.backend.controller.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import be.ap.backend.dto.CurrentUserDTO;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/auth")
public class CurrentUserController {

    @GetMapping("/current-user")
    public ResponseEntity<CurrentUserDTO> getCurrentUser(HttpSession session) {
        Object userRaw = session.getAttribute("userId");
        if (userRaw == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Niet ingelogd.");
        }
        CurrentUserDTO currentUser = new CurrentUserDTO();
        currentUser.setUserId(toLong(session.getAttribute("userId")));
        currentUser.setRole((String) session.getAttribute("role"));
        currentUser.setSchoolId(toLong(session.getAttribute("school")));
        currentUser.setLocationId(toLong(session.getAttribute("location")));
        currentUser.setFirstName((String) session.getAttribute("firstName"));
        currentUser.setLastName((String) session.getAttribute("lastName"));
        currentUser.setEmail((String) session.getAttribute("email"));
        currentUser.setUsername((String) session.getAttribute("username"));
        return ResponseEntity.ok(currentUser);
    }

    private Long toLong(Object value) {
        if (value == null)
            return null;
        if (value instanceof Long l)
            return l;
        if (value instanceof Number n)
            return n.longValue();
        String s = value.toString().trim();
        return s.isBlank() ? null : Long.valueOf(s);
    }
}