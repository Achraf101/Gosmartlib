package be.ap.backend.controller;

import be.ap.backend.dto.StudentReportDTO;
import be.ap.backend.service.ClassroomService;
import be.ap.backend.service.StudentReportService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebMvcTest(StudentReportController.class)
public class StudentReportControllerTest {

    @MockitoBean
    private StudentReportService studentReportService;

    @MockitoBean
    private ClassroomService classroomService;

    @Autowired
    private StudentReportController studentReportController;

    // ── Session helpers ────────────────────────────────────────────

    private HttpSession teacherSession() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("role")).thenReturn("LEERKRACHT");
        when(session.getAttribute("userId")).thenReturn("1");
        return session;
    }

    private HttpSession adminSession() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(session.getAttribute("userId")).thenReturn("2");
        return session;
    }

    private HttpSession studentSession() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("role")).thenReturn("LEERLING");
        when(session.getAttribute("userId")).thenReturn("3");
        return session;
    }

    /** Simulates a session with no attributes set (unauthenticated). */
    private HttpSession emptySession() {
        return mock(HttpSession.class);
    }

    // ── getReport ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /students/{id}/report — teacher with class access returns 200 and report body")
    void getReport_teacherWithAccess_returns200() {
        StudentReportDTO report = new StudentReportDTO();
        report.setStudentId(5L);

        when(classroomService.teacherCanViewStudent(1L, 5L)).thenReturn(true);
        when(studentReportService.buildReport(5L)).thenReturn(report);

        var response = studentReportController.getReport(5L, teacherSession());

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStudentId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("GET /students/{id}/report — teacher without access to student returns 403")
    void getReport_studentNotInTeachersClass_returns403() {
        when(classroomService.teacherCanViewStudent(1L, 99L)).thenReturn(false);

        assertThatThrownBy(() -> studentReportController.getReport(99L, teacherSession()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    @DisplayName("GET /students/{id}/report — admin role (non-teacher) returns 403")
    void getReport_adminRole_returns403() {
        // Role guard fires before any classroom lookup; no classroomService stub
        // needed.
        assertThatThrownBy(() -> studentReportController.getReport(5L, adminSession()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    @DisplayName("GET /students/{id}/report — student role (non-teacher) returns 403")
    void getReport_studentRole_returns403() {
        assertThatThrownBy(() -> studentReportController.getReport(5L, studentSession()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    @DisplayName("GET /students/{id}/report — unauthenticated session returns 401")
    void getReport_notLoggedIn_returns401() {
        // emptySession() returns null for all attributes; both role and userId are
        // absent.
        assertThatThrownBy(() -> studentReportController.getReport(5L, emptySession()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
    }

    @Test
    @DisplayName("GET /students/{id}/report — session with role but no userId returns 401")
    void getReport_missingUserId_returns401() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("role")).thenReturn("LEERKRACHT");
        // userId deliberately absent — returns null

        assertThatThrownBy(() -> studentReportController.getReport(5L, session))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
    }
}