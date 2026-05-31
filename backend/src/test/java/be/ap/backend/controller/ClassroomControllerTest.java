package be.ap.backend.controller;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.ClassroomDTO;
import be.ap.backend.dto.StudentPreviewDTO;
import be.ap.backend.entity.UserRole;
import be.ap.backend.service.ClassroomService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@WebMvcTest(ClassroomController.class)
public class ClassroomControllerTest {

    @MockitoBean
    private ClassroomService classroomService;

    @MockitoBean
    private SessionContext sessionContext;

    @Autowired
    private ClassroomController classroomController;

    private void asTeacher(Long userId) {
        when(sessionContext.hasRole(UserRole.LEERKRACHT)).thenReturn(true);
        when(sessionContext.getUserId()).thenReturn(userId);
    }

    private void asStudent() {
        when(sessionContext.hasRole(UserRole.LEERKRACHT)).thenReturn(false);
    }

    private void notLoggedIn() {
        when(sessionContext.hasRole(UserRole.LEERKRACHT)).thenReturn(false);
        when(sessionContext.getUserId()).thenReturn(null);
    }

    // ── getMyClassrooms ───────────────────────────────────────────

    @Test
    void getMyClassrooms_asTeacher_returns200WithList() {
        asTeacher(1L);
        when(classroomService.getClassroomsForTeacher(1L))
                .thenReturn(List.of(new ClassroomDTO(10L, "3A", 3)));

        var response = classroomController.getMyClassrooms();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getName()).isEqualTo("3A");
    }

    @Test
    void getMyClassrooms_asTeacher_emptyList_returns200() {
        asTeacher(1L);
        when(classroomService.getClassroomsForTeacher(1L)).thenReturn(List.of());

        var response = classroomController.getMyClassrooms();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getMyClassrooms_notTeacher_returns403() {
        asStudent();

        assertThatThrownBy(() -> classroomController.getMyClassrooms())
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    void getMyClassrooms_notLoggedIn_returns403() {
        notLoggedIn();

        assertThatThrownBy(() -> classroomController.getMyClassrooms())
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    // NEW: teacher role present but getUserId() returns null
    @Test
    void getMyClassrooms_teacherRoleButNullUserId_returns403() {
        when(sessionContext.hasRole(UserRole.LEERKRACHT)).thenReturn(true);
        when(sessionContext.getUserId()).thenReturn(null);

        assertThatThrownBy(() -> classroomController.getMyClassrooms())
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    // ── getStudents ───────────────────────────────────────────────

    @Test
    void getStudents_asTeacher_returns200WithStudents() {
        asTeacher(1L);
        when(classroomService.getStudentsForClassroom(1L, 10L))
                .thenReturn(List.of(new StudentPreviewDTO(2L, "Anna", "De Smet", null)));

        var response = classroomController.getStudents(10L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getId()).isEqualTo(2L);
        assertThat(response.getBody().get(0).getFirstName()).isEqualTo("Anna");
        assertThat(response.getBody().get(0).getLastName()).isEqualTo("De Smet");
    }

    // NEW: classroom exists but has no students
    @Test
    void getStudents_asTeacher_emptyClassroom_returns200() {
        asTeacher(1L);
        when(classroomService.getStudentsForClassroom(1L, 10L)).thenReturn(List.of());

        var response = classroomController.getStudents(10L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getStudents_wrongClassroom_returns403() {
        asTeacher(1L);
        when(classroomService.getStudentsForClassroom(1L, 99L))
                .thenThrow(new SecurityException("niet de leerkracht"));

        assertThatThrownBy(() -> classroomController.getStudents(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    void getStudents_classroomNotFound_propagatesEntityNotFoundException() {
        asTeacher(1L);
        when(classroomService.getStudentsForClassroom(1L, 99L))
                .thenThrow(new EntityNotFoundException("Klas niet gevonden"));

        assertThatThrownBy(() -> classroomController.getStudents(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Klas niet gevonden");
    }

    @Test
    void getStudents_notTeacher_returns403() {
        asStudent();

        assertThatThrownBy(() -> classroomController.getStudents(10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    void getStudents_notLoggedIn_returns403() {
        notLoggedIn();

        assertThatThrownBy(() -> classroomController.getStudents(10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    // NEW: teacher role present but getUserId() returns null
    @Test
    void getStudents_teacherRoleButNullUserId_returns403() {
        when(sessionContext.hasRole(UserRole.LEERKRACHT)).thenReturn(true);
        when(sessionContext.getUserId()).thenReturn(null);

        assertThatThrownBy(() -> classroomController.getStudents(10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }
}
