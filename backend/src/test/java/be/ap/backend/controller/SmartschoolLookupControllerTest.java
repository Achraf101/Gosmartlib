package be.ap.backend.controller;

import be.ap.backend.dto.TeacherDTO;
import be.ap.backend.entity.UserRole;
import be.ap.backend.service.SmartschoolLookupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmartschoolLookupControllerTest {

    @Mock
    private SmartschoolLookupService lookupService;

    @InjectMocks
    private SmartschoolLookupController controller;

    // -------------------------------------------------------------------------
    // getUser
    // -------------------------------------------------------------------------

    @Test
    void getUser_returnsOk_whenUserFound() {
        Long schoolId = 1L;
        String ssId = "user-42";
        Map<String, Object> userData = Map.of("id", ssId, "role", "student");

        when(lookupService.getUser(schoolId, ssId, Set.of(UserRole.STUDENT)))
                .thenReturn(userData);

        ResponseEntity<Map<String, Object>> response = controller.getUser(schoolId, ssId, "student");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(userData);
        verify(lookupService).getUser(schoolId, ssId, Set.of(UserRole.STUDENT));
    }

    @Test
    void getUser_returnsNotFound_whenServiceReturnsNull() {
        Long schoolId = 1L;
        String ssId = "user-99";

        when(lookupService.getUser(schoolId, ssId, Set.of(UserRole.LEERKRACHT)))
                .thenReturn(null);

        ResponseEntity<Map<String, Object>> response = controller.getUser(schoolId, ssId, "teacher");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getUser_mapsStudentRole_toStudentUserRole() {
        Long schoolId = 1L;
        String ssId = "user-7";

        when(lookupService.getUser(schoolId, ssId, Set.of(UserRole.STUDENT)))
                .thenReturn(Map.of("id", ssId));

        controller.getUser(schoolId, ssId, "student");

        verify(lookupService).getUser(schoolId, ssId, Set.of(UserRole.STUDENT));
        verifyNoMoreInteractions(lookupService);
    }

    @Test
    void getUser_mapsNonStudentRole_toLeerkrachtUserRole() {
        Long schoolId = 1L;
        String ssId = "user-8";

        when(lookupService.getUser(schoolId, ssId, Set.of(UserRole.LEERKRACHT)))
                .thenReturn(Map.of("id", ssId));

        controller.getUser(schoolId, ssId, "admin");

        verify(lookupService).getUser(schoolId, ssId, Set.of(UserRole.LEERKRACHT));
        verifyNoMoreInteractions(lookupService);
    }

    @Test
    void getUser_roleComparison_isCaseInsensitive() {
        Long schoolId = 1L;
        String ssId = "user-9";

        when(lookupService.getUser(schoolId, ssId, Set.of(UserRole.STUDENT)))
                .thenReturn(Map.of("id", ssId));

        controller.getUser(schoolId, ssId, "STUDENT");

        verify(lookupService).getUser(schoolId, ssId, Set.of(UserRole.STUDENT));
    }

    @Test
    void getUser_passesCorrectSchoolIdToService() {
        Long schoolId = 42L;
        String ssId = "user-10";

        when(lookupService.getUser(schoolId, ssId, Set.of(UserRole.STUDENT)))
                .thenReturn(Map.of("id", ssId));

        controller.getUser(schoolId, ssId, "student");

        verify(lookupService).getUser(schoolId, ssId, Set.of(UserRole.STUDENT));
    }

    // -------------------------------------------------------------------------
    // getClass (classroom lookup)
    // -------------------------------------------------------------------------

    @Test
    void getClass_returnsOk_whenClassroomFound() {
        Long schoolId = 2L;
        String ssId = "class-A1";
        Map<String, Object> classData = Map.of("id", ssId, "name", "1A");

        when(lookupService.getClassroom(schoolId, ssId)).thenReturn(classData);

        ResponseEntity<Map<String, Object>> response = controller.getClass(schoolId, ssId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(classData);
        verify(lookupService).getClassroom(schoolId, ssId);
    }

    @Test
    void getClass_returnsNotFound_whenServiceReturnsNull() {
        Long schoolId = 2L;
        String ssId = "class-unknown";

        when(lookupService.getClassroom(schoolId, ssId)).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = controller.getClass(schoolId, ssId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getClass_delegatesCorrectArgumentsToService() {
        Long schoolId = 2L;
        String ssId = "class-B2";

        when(lookupService.getClassroom(schoolId, ssId))
                .thenReturn(Map.of("id", ssId));

        controller.getClass(schoolId, ssId);

        verify(lookupService).getClassroom(schoolId, ssId);
        verifyNoMoreInteractions(lookupService);
    }

    // -------------------------------------------------------------------------
    // getAllTeachersForSchool
    // -------------------------------------------------------------------------

    @Test
    void getAllTeachersForSchool_returnsOk_withTeacherList() {
        Long schoolId = 3L;
        List<TeacherDTO> teachers = List.of(mock(TeacherDTO.class), mock(TeacherDTO.class));

        when(lookupService.getAllTeachersForSchool(schoolId)).thenReturn(teachers);

        ResponseEntity<List<TeacherDTO>> response = controller.getAllTeachersForSchool(schoolId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(teachers);
        verify(lookupService).getAllTeachersForSchool(schoolId);
    }

    @Test
    void getAllTeachersForSchool_returnsOk_withEmptyList() {
        Long schoolId = 3L;

        when(lookupService.getAllTeachersForSchool(schoolId)).thenReturn(List.of());

        ResponseEntity<List<TeacherDTO>> response = controller.getAllTeachersForSchool(schoolId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getAllTeachersForSchool_delegatesCorrectSchoolIdToService() {
        Long schoolId = 3L;

        when(lookupService.getAllTeachersForSchool(schoolId)).thenReturn(List.of());

        controller.getAllTeachersForSchool(schoolId);

        verify(lookupService).getAllTeachersForSchool(schoolId);
        verifyNoMoreInteractions(lookupService);
    }
}