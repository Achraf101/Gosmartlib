package be.ap.backend.controller;

import be.ap.backend.dto.StudentReportDTO;
import be.ap.backend.service.ClassroomService;
import be.ap.backend.service.StudentReportService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class StudentReportControllerTest {

    @MockitoBean
    private StudentReportService studentReportService;

    @MockitoBean
    private ClassroomService classroomService;

    @Autowired
    private StudentReportController studentReportController;

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

    private HttpSession emptySession() {
        return mock(HttpSession.class);
    }

    // ── getReport ─────────────────────────────────────────────────

    @Test
    void getReport_teacherWithAccess_returns200() {
        StudentReportDTO report = new StudentReportDTO();
        report.setStudentId(5L);

        when(classroomService.teacherCanViewStudent(1L, 5L)).thenReturn(true);
        when(studentReportService.buildReport(5L)).thenReturn(report);

        var response = studentReportController.getReport(5L, teacherSession());

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getStudentId()).isEqualTo(5L);
    }

    @Test
    void getReport_studentNotInTeachersClass_returns403() {
        when(classroomService.teacherCanViewStudent(1L, 99L)).thenReturn(false);

        assertThatThrownBy(() -> studentReportController.getReport(99L, teacherSession()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    void getReport_notTeacher_returns403() {
        assertThatThrownBy(() -> studentReportController.getReport(5L, adminSession()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    void getReport_notLoggedIn_returns401() {
        assertThatThrownBy(() -> studentReportController.getReport(5L, emptySession()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
    }
}
