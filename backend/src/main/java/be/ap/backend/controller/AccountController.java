package be.ap.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.PasswordDTO;
import be.ap.backend.service.AccountService;
import lombok.RequiredArgsConstructor;

/**
 * Handles account-related operations for authenticated users.
 */
@RestController
@RequestMapping("account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final SessionContext session;

    /**
     * Updates the password of a user.
     *
     * <p>
     * Only users with the ADMIN role are allowed to perform this operation.
     * </p>
     *
     * @param dto request body containing the new password data
     * @return empty HTTP 200 response if the update succeeds
     */
    @PutMapping("password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updatePassword(@RequestBody PasswordDTO dto) {

        Long userId = (Long) session.getUserId();
        accountService.updatePassword(userId, dto);
        return ResponseEntity.ok().build();
    }
}
