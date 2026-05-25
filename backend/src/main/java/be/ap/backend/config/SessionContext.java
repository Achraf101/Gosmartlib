package be.ap.backend.config;

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

    // needed fields: id, role, name, location_id
    public Long getUserId() {
        return (Long) session.getAttribute("userId");
    }

    public void setUserId(Long id) {
        session.setAttribute("userId", id);
    }

    public UserRole getRole() {
        String role = (String) session.getAttribute("role");
        if (role == null) return null;
        return UserRole.valueOf(role);
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

    public Long getLocationId() {
        return (Long) session.getAttribute("locationId");
    }

    public void setLocationId(Long id) {
        session.setAttribute("locationId", id);
    }

    public Long getSchoolId() {
        Object schoolId = session.getAttribute("school");
        if (schoolId == null) return null;
        return Long.parseLong(schoolId.toString());
   }

    public void setSchoolId(Long id) {
        session.setAttribute("schoolId", id);
    }
}