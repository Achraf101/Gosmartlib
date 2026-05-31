package be.ap.backend.controller;

import be.ap.backend.dto.AddRoleRequestDTO;
import be.ap.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing user administration operations.
 *
 * <p>
 * Provides endpoints for modifying user roles. Access is restricted
 * to administrators and library managers via method-level security.
 * </p>
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Adds a role to a specific user.
     *
     * <p>
     * Only users with ADMIN or BIBLIOTHEEKBEHEERDER roles are authorized
     * to perform this operation.
     * </p>
     *
     * @param userId  the ID of the user to modify
     * @param request the role assignment request
     * @return no content on success
     */
    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTHEEKBEHEERDER')")
    public ResponseEntity<Void> addRole(
            @PathVariable Long userId,
            @RequestBody AddRoleRequestDTO request) {
        userService.addRole(userId, request.getRole());
        return ResponseEntity.noContent().build();
    }
}