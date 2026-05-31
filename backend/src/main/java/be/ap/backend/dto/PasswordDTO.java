package be.ap.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO carrying a password change request.
 */
@Data
@AllArgsConstructor
public class PasswordDTO {
    private String currentPassword;
    private String newPassword;
}
