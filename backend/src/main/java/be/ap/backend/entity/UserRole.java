package be.ap.backend.entity;

import org.springframework.security.core.GrantedAuthority;

/**
 * Represents the roles available in the system and maps them to Spring Security
 * authorities.
 *
 * <p>
 * Each role is automatically prefixed with "ROLE_" to comply with Spring
 * Security conventions.
 * </p>
 */
public enum UserRole implements GrantedAuthority {
    ADMIN,
    BIBLIOTHEEKBEHEERDER,
    LEERKRACHT,
    STUDENT;

    @Override
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}