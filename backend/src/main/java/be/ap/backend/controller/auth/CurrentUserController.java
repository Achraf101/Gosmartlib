package be.ap.backend.controller.auth;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.CurrentUserDTO;
import be.ap.backend.entity.UserRole;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class CurrentUserController {

    private final SessionContext sessionContext;

    @GetMapping("/current-user")
    public ResponseEntity<CurrentUserDTO> getCurrentUser() {
        if (sessionContext.getUserId() == null) {
            // throw new MissingSessionException("Niet ingelogd.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Niet ingelogd.");
        }
        CurrentUserDTO currentUser = new CurrentUserDTO();
        currentUser.setUserId(sessionContext.getUserId());
        currentUser.setRoles(sessionContext.getRoles().stream()
                .map(UserRole::name)
                .collect(Collectors.toSet()));
        currentUser.setSchoolId(sessionContext.getSchoolId());
        currentUser.setFirstName(sessionContext.getFirstName());
        currentUser.setLastName(sessionContext.getLastName());
        currentUser.setEmail(sessionContext.getEmail());
        currentUser.setUsername(sessionContext.getUsername());
        return ResponseEntity.ok(currentUser);
    }
}