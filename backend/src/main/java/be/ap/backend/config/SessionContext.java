package be.ap.backend.config;

import java.util.Set;

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

    // needed fields: id, role, name
    public Long getUserId() {
        return (Long) session.getAttribute("userId");
    }

    public void setUserId(Long id) {
        session.setAttribute("userId", id);
    }

    public UserRole getRole() {
        @SuppressWarnings("unchecked")
        Set<String> roles = (Set<String>) session.getAttribute("roles");
        if (roles == null || roles.isEmpty()) return null;
        return UserRole.valueOf(roles.iterator().next());
    }

    public void setRole(UserRole role) {
        session.setAttribute("role", role);
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