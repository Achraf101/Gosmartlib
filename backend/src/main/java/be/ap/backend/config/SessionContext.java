package be.ap.backend.config;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import be.ap.backend.entity.UserRole;
import jakarta.servlet.http.HttpSession;

/**
 * Request-scoped wrapper around the HTTP session.
 *
 * <p>
 * Provides typed access to authentication-related session attributes
 * stored during login.
 * </p>
 */
@Component
@RequestScope
public class SessionContext {

    private final HttpSession session;

    public SessionContext(HttpSession session) {
        this.session = session;
    }

    /**
     * Retrieves the authenticated user's ID from the session.
     *
     * @return user ID if present, otherwise {@code null}
     */
    public Long getUserId() {
        Object raw = session.getAttribute("userId");
        if (!(raw instanceof Long))
            return null;
        return (Long) raw;
    }

    /**
     * Stores the authenticated user's ID in the session.
     *
     * @param id the user ID to store
     */
    public void setUserId(Long id) {
        session.setAttribute("userId", id);
    }

    /**
     * Retrieves the user's roles from the session and converts them to
     * {@link UserRole}.
     *
     * @return roles of the user, or an empty set if none are present
     */
    public Set<UserRole> getRoles() {
        Object raw = session.getAttribute("roles");
        if (!(raw instanceof Set<?>)) {
            return Set.of();
        }
        return ((Set<?>) raw).stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(UserRole::valueOf)
                .collect(Collectors.toSet());

    }

    public boolean hasRole(UserRole role) {
        return getRoles().contains(role);
    }

    /**
     * Stores the user's roles in the session.
     *
     * @param roles roles to store
     */
    public void setRole(Set<UserRole> roles) {
        Set<String> roleStrings = roles.stream()
                .map(UserRole::name)
                .collect(Collectors.toSet());
        session.setAttribute("roles", roleStrings);
    }

    public String getName() {
        return (String) session.getAttribute("name");
    }

    public void setName(String name) {
        session.setAttribute("name", name);
    }

    /**
     * Haalt de school-ID op uit de sessie.
     *
     * @return de school-ID of {@code null} indien niet aanwezig
     */
    public Long getSchoolId() {
        Object schoolId = session.getAttribute("school");
        if (schoolId == null)
            return null;
        return Long.parseLong(schoolId.toString());
    }

    public void setSchoolId(Long id) {
        session.setAttribute("school", id);
    }

    public String getFirstName() {
        return (String) session.getAttribute("firstName");
    }

    public void setFirstName(String v) {
        session.setAttribute("firstName", v);
    }

    public String getLastName() {
        return (String) session.getAttribute("lastName");
    }

    public void setLastName(String v) {
        session.setAttribute("lastName", v);
    }

    public String getEmail() {
        return (String) session.getAttribute("email");
    }

    public void setEmail(String v) {
        session.setAttribute("email", v);
    }

    public String getUsername() {
        return (String) session.getAttribute("username");
    }

    public void setUsername(String v) {
        session.setAttribute("username", v);
    }
}