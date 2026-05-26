package be.ap.backend.dto;

import be.ap.backend.entity.UserRole;
import lombok.Data;

@Data
public class AddRoleRequestDTO {
    private UserRole role;
}