package be.ap.backend.config;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import be.ap.backend.entity.UserRole;
import jakarta.servlet.http.HttpSession;

@Component
@RequestScope
public class SessionContext {

    private final HttpSession session;

    public SessionContext(HttpSession session) {
        this.session = session;
    }

    
    public Long getUserId() {
        return (Long) session.getAttribute("userId");
    }

    public void setUserId(Long id) {
        session.setAttribute("userId", id);
    }

    public Set<UserRole> getRoles() {
        @SuppressWarnings("unchecked")
        Set<String> roleStrings = (Set<String>) session.getAttribute("roles");
        if (roleStrings == null || roleStrings.isEmpty()) {
            return Set.of();
        }
        return roleStrings.stream()
                .map(UserRole::valueOf)
                .collect(Collectors.toSet());
    }

    public boolean hasRole(UserRole role) {
        return getRoles().contains(role);
    }

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

    public Long getSchoolId() {
        Object schoolId = session.getAttribute("school");
        if (schoolId == null) return null;
        return Long.parseLong(schoolId.toString());
   }

    public void setSchoolId(Long id) {
        session.setAttribute("school", id);
    }
}