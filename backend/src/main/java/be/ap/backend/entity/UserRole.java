package be.ap.backend.entity;

import org.springframework.security.core.GrantedAuthority;

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
