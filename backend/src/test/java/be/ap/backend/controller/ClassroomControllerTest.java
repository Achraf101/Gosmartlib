package be.ap.backend.controller;

import be.ap.backend.dto.ClassroomDTO;
import be.ap.backend.dto.StudentPreviewDTO;
import be.ap.backend.service.ClassroomService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebMvcTest(ClassroomController.class)
public class ClassroomControllerTest {

    @MockitoBean
    private ClassroomService classroomService;

    @Autowired
    private ClassroomController classroomController;

    private HttpSession teacherSession() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("role")).thenReturn("LEERKRACHT");
        when(session.getAttribute("userId")).thenReturn("1");
        return session;
    }

    private HttpSession studentSession() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("role")).thenReturn("STUDENT");
        when(session.getAttribute("userId")).thenReturn("5");
        return session;
    }

    private HttpSession emptySession() {
        return mock(HttpSession.class);
    }

    // ── getMyClassrooms ───────────────────────────────────────────

    @Test
    void getMyClassrooms_asTeacher_returns200WithList() {
        when(classroomService.getClassroomsForTeacher(1L))
                .thenReturn(List.of(new ClassroomDTO(10L, "3A", 3)));

        var response = classroomController.getMyClassrooms(teacherSession());

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getName()).isEqualTo("3A");
    }

    @Test
    void getMyClassrooms_asTeacher_emptyList_returns200() {
        when(classroomService.getClassroomsForTeacher(1L)).thenReturn(List.of());

        var response = classroomController.getMyClassrooms(teacherSession());

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getMyClassrooms_notTeacher_returns403() {
        assertThatThrownBy(() -> classroomController.getMyClassrooms(studentSession()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    void getMyClassrooms_notLoggedIn_returns401() {
        assertThatThrownBy(() -> classroomController.getMyClassrooms(emptySession()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
    }

    // ── getStudents ───────────────────────────────────────────────

    @Test
    void getStudents_asTeacher_returns200WithStudents() {
        when(classroomService.getStudentsForClassroom(1L, 10L))
                .thenReturn(List.of(new StudentPreviewDTO(2L, "anna", "Anna", null)));

        var response = classroomController.getStudents(10L, teacherSession());

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getStudents_wrongClassroom_returns403() {
        when(classroomService.getStudentsForClassroom(1L, 99L))
                .thenThrow(new SecurityException("niet de leerkracht"));

        assertThatThrownBy(() -> classroomController.getStudents(99L, teacherSession()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    void getStudents_classroomNotFound_propagatesEntityNotFoundException() {
        when(classroomService.getStudentsForClassroom(1L, 99L))
                .thenThrow(new EntityNotFoundException("Klas niet gevonden"));

        assertThatThrownBy(() -> classroomController.getStudents(99L, teacherSession()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Klas niet gevonden");
    }

    @Test
    void getStudents_notTeacher_returns403() {
        assertThatThrownBy(() -> classroomController.getStudents(10L, studentSession()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    void getStudents_notLoggedIn_returns401() {
        assertThatThrownBy(() -> classroomController.getStudents(10L, emptySession()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
    }
}