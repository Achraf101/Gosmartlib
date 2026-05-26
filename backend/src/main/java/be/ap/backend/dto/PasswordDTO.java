package be.ap.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordDTO {
    private String currentPassword;
    private String newPassword;
}
