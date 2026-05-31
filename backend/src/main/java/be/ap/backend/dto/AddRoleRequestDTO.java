package be.ap.backend.dto;

import be.ap.backend.entity.UserRole;
import lombok.Data;

/**
 * Data transfer object for requesting the assignment of a role to a user.
 *
 * <p>
 * Encapsulates a single {@link UserRole} that will be added to a user account.
 * Used by administrative endpoints responsible for role management.
 * </p>
 */
@Data
public class AddRoleRequestDTO {
    private UserRole role;
}