package be.ap.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.config.SessionContext;
import be.ap.backend.entity.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
@Slf4j
public class SessionDemoController {

    private final SessionContext sessionContext;

    @PostMapping("/{role}")
    public ResponseEntity<?> updateRoleDemo(@PathVariable String role, HttpServletRequest request) {
        log.debug(role);
        Set<UserRole> roles;

        if (role.equals("BIBLIOTHEEKBEHEERDER")) {
            roles = Set.of(UserRole.valueOf("BIBLIOTHEEKBEHEERDER"), UserRole.valueOf("LEERKRACHT"));
        } else if (role.equals("LEERKRACHT")) {
            roles = Set.of(UserRole.valueOf("LEERKRACHT"));
        } else {
            roles = Set.of(UserRole.valueOf("ADMIN"));
        }
        sessionContext.setRole(roles);

        return ResponseEntity.ok("Role updated");
    }

}
