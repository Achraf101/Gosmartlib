package be.ap.backend.controller;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.StudentReportDTO;
import be.ap.backend.entity.UserRole;
import be.ap.backend.exception.MissingSessionException;
import be.ap.backend.exception.UnauthorizedAccessException;
import be.ap.backend.service.ClassroomService;
import be.ap.backend.service.StudentReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@WebMvcTest(StudentReportController.class)
public class StudentReportControllerTest {

    @MockitoBean
    private StudentReportService studentReportService;

    @MockitoBean
    private ClassroomService classroomService;

    @MockitoBean
    private SessionContext sessionContext;

    @Autowired
    private StudentReportController studentReportController;

    private void asTeacher(Long userId) {
        when(sessionContext.getUserId()).thenReturn(userId);
        when(sessionContext.hasRole(UserRole.LEERKRACHT)).thenReturn(true);
    }

    private void asNonTeacher(Long userId) {
        when(sessionContext.getUserId()).thenReturn(userId);
        when(sessionContext.hasRole(UserRole.LEERKRACHT)).thenReturn(false);
    }

    private void notLoggedIn() {
        when(sessionContext.getUserId()).thenReturn(null);
        when(sessionContext.hasRole(UserRole.LEERKRACHT)).thenReturn(false);
    }

    // ── getReport ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /students/{id}/report — teacher with class access returns 200 and report body")
    void getReport_teacherWithAccess_returns200() {
        asTeacher(1L);
        StudentReportDTO report = new StudentReportDTO();
        report.setStudentId(5L);

        when(classroomService.teacherCanViewStudent(1L, 5L)).thenReturn(true);
        when(studentReportService.buildReport(5L)).thenReturn(report);

        var response = studentReportController.getReport(5L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStudentId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("GET /students/{id}/report — teacher without access to student throws UnauthorizedAccessException")
    void getReport_studentNotInTeachersClass_throwsUnauthorizedAccessException() {
        asTeacher(1L);
        when(classroomService.teacherCanViewStudent(1L, 99L)).thenReturn(false);

        assertThatThrownBy(() -> studentReportController.getReport(99L))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    @DisplayName("GET /students/{id}/report — admin role (non-teacher) throws MissingSessionException")
    void getReport_adminRole_throwsMissingSessionException() {
        asNonTeacher(2L);

        assertThatThrownBy(() -> studentReportController.getReport(5L))
                .isInstanceOf(MissingSessionException.class);
    }

    @Test
    @DisplayName("GET /students/{id}/report — student role (non-teacher) throws MissingSessionException")
    void getReport_studentRole_throwsMissingSessionException() {
        asNonTeacher(3L);

        assertThatThrownBy(() -> studentReportController.getReport(5L))
                .isInstanceOf(MissingSessionException.class);
    }

    @Test
    @DisplayName("GET /students/{id}/report — unauthenticated session throws MissingSessionException")
    void getReport_notLoggedIn_throwsMissingSessionException() {
        notLoggedIn();

        assertThatThrownBy(() -> studentReportController.getReport(5L))
                .isInstanceOf(MissingSessionException.class);
    }

    @Test
    @DisplayName("GET /students/{id}/report — session with role but no userId throws MissingSessionException")
    void getReport_missingUserId_throwsMissingSessionException() {
        when(sessionContext.getUserId()).thenReturn(null);
        when(sessionContext.hasRole(UserRole.LEERKRACHT)).thenReturn(true);

        assertThatThrownBy(() -> studentReportController.getReport(5L))
                .isInstanceOf(MissingSessionException.class);
    }
}