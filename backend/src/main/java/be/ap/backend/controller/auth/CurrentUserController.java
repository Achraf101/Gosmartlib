package be.ap.backend.controller.auth;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.CurrentUserDTO;
import be.ap.backend.entity.UserRole;
import be.ap.backend.exception.MissingSessionException;
import lombok.RequiredArgsConstructor;

/**
 * Exposes information about the currently authenticated user based on the HTTP
 * session.
 *
 * <p>
 * Returns a DTO containing identity and profile data stored in the session
 * context.
 * </p>
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class CurrentUserController {

    private final SessionContext sessionContext;

    /**
     * Retrieves the current authenticated user from the session.
     *
     * <p>
     * Throws an exception if no valid session is present.
     * </p>
     *
     * @return DTO containing the current user's session data
     */
    @GetMapping("/current-user")
    public ResponseEntity<CurrentUserDTO> getCurrentUser() {
        if (sessionContext.getUserId() == null) {
            throw new MissingSessionException("Niet ingelogd.");
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