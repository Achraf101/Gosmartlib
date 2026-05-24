package be.ap.backend.controller;

import be.ap.backend.dto.AddRoleRequestDTO;
import be.ap.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTHEEKBEHEERDER')")
    public ResponseEntity<Void> addRole(
            @PathVariable Long userId,
            @RequestBody AddRoleRequestDTO request) {
        userService.addRole(userId, request.getRole());
        return ResponseEntity.noContent().build();
    }
}