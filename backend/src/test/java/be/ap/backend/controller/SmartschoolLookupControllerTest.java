package be.ap.backend.controller;

import be.ap.backend.dto.TeacherDTO;
import be.ap.backend.entity.School;
import be.ap.backend.entity.UserRole;
import be.ap.backend.repository.SchoolRepository;
import be.ap.backend.service.SmartschoolLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmartschoolLookupControllerTest {

    @Mock
    private SmartschoolLookupService lookupService;

    @Mock
    private SchoolRepository schoolRepository;

    @InjectMocks
    private SmartschoolLookupController controller;

    private School school;

    @BeforeEach
    void setUp() {
        school = new School();
    }

    // -------------------------------------------------------------------------
    // getUser
    // -------------------------------------------------------------------------

    @Test
    void getUser_returnsOk_whenUserFound() {
        Long schoolId = 1L;
        String ssId = "user-42";
        Map<String, Object> userData = Map.of("id", ssId, "role", "student");

        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
        when(lookupService.getUser(school, ssId, Set.of(UserRole.STUDENT))).thenReturn(userData);

        ResponseEntity<Map<String, Object>> response = controller.getUser(schoolId, ssId, "student");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(userData);
        verify(lookupService).getUser(school, ssId, Set.of(UserRole.STUDENT));
    }

    @Test
    void getUser_returnsNotFound_whenServiceReturnsNull() {
        Long schoolId = 1L;
        String ssId = "user-99";

        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
        when(lookupService.getUser(school, ssId, Set.of(UserRole.LEERKRACHT))).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = controller.getUser(schoolId, ssId, "teacher");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getUser_throwsRuntimeException_whenSchoolNotFound() {
        Long schoolId = 999L;
        when(schoolRepository.findById(schoolId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getUser(schoolId, "any-ss-id", "student"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("School not found: 999");

        verifyNoInteractions(lookupService);
    }

    @Test
    void getUser_mapsStudentRole_toStudentUserRole() {
        Long schoolId = 1L;
        String ssId = "user-7";

        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
        when(lookupService.getUser(school, ssId, Set.of(UserRole.STUDENT))).thenReturn(Map.of("id", ssId));

        controller.getUser(schoolId, ssId, "student");

        verify(lookupService).getUser(school, ssId, Set.of(UserRole.STUDENT));
        verifyNoMoreInteractions(lookupService);
    }

    @Test
    void getUser_mapsNonStudentRole_toLeerkrachtUserRole() {
        // Any role string other than "student" (case-insensitive) maps to LEERKRACHT.
        Long schoolId = 1L;
        String ssId = "user-8";

        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
        when(lookupService.getUser(school, ssId, Set.of(UserRole.LEERKRACHT))).thenReturn(Map.of("id", ssId));

        controller.getUser(schoolId, ssId, "admin");

        verify(lookupService).getUser(school, ssId, Set.of(UserRole.LEERKRACHT));
        verifyNoMoreInteractions(lookupService);
    }

    @Test
    void getUser_roleComparison_isCaseInsensitive() {
        // "STUDENT" in uppercase must still resolve to UserRole.STUDENT.
        Long schoolId = 1L;
        String ssId = "user-9";

        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
        when(lookupService.getUser(school, ssId, Set.of(UserRole.STUDENT))).thenReturn(Map.of("id", ssId));

        controller.getUser(schoolId, ssId, "STUDENT");

        verify(lookupService).getUser(school, ssId, Set.of(UserRole.STUDENT));
    }

    // -------------------------------------------------------------------------
    // getClass (classroom lookup)
    // -------------------------------------------------------------------------

    @Test
    void getClass_returnsOk_whenClassroomFound() {
        Long schoolId = 2L;
        String ssId = "class-A1";
        Map<String, Object> classData = Map.of("id", ssId, "name", "1A");

        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
        when(lookupService.getClassroom(school, ssId)).thenReturn(classData);

        ResponseEntity<Map<String, Object>> response = controller.getClass(schoolId, ssId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(classData);
        verify(lookupService).getClassroom(school, ssId);
    }

    @Test
    void getClass_returnsNotFound_whenServiceReturnsNull() {
        Long schoolId = 2L;
        String ssId = "class-unknown";

        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
        when(lookupService.getClassroom(school, ssId)).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = controller.getClass(schoolId, ssId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getClass_throwsRuntimeException_whenSchoolNotFound() {
        Long schoolId = 888L;
        when(schoolRepository.findById(schoolId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getClass(schoolId, "class-X"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("School not found: 888");

        verifyNoInteractions(lookupService);
    }

    @Test
    void getClass_delegatesCorrectSsIdToService() {
        Long schoolId = 2L;
        String ssId = "class-B2";

        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
        when(lookupService.getClassroom(school, ssId)).thenReturn(Map.of("id", ssId));

        controller.getClass(schoolId, ssId);

        verify(lookupService).getClassroom(school, ssId);
        verifyNoMoreInteractions(lookupService);
    }

    // -------------------------------------------------------------------------
    // getAllTeachersForSchool
    // -------------------------------------------------------------------------

    @Test
    void getAllTeachersForSchool_returnsOk_withTeacherList() {
        Long schoolId = 3L;
        List<TeacherDTO> teachers = List.of(mock(TeacherDTO.class), mock(TeacherDTO.class));

        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
        when(lookupService.getAllTeachersForSchool(school)).thenReturn(teachers);

        ResponseEntity<List<TeacherDTO>> response = controller.getAllTeachersForSchool(schoolId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(teachers);
        verify(lookupService).getAllTeachersForSchool(school);
    }

    @Test
    void getAllTeachersForSchool_returnsOk_withEmptyList() {
        Long schoolId = 3L;

        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
        when(lookupService.getAllTeachersForSchool(school)).thenReturn(List.of());

        ResponseEntity<List<TeacherDTO>> response = controller.getAllTeachersForSchool(schoolId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getAllTeachersForSchool_throwsRuntimeException_whenSchoolNotFound() {
        Long schoolId = 777L;
        when(schoolRepository.findById(schoolId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getAllTeachersForSchool(schoolId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("School not found: 777");

        verifyNoInteractions(lookupService);
    }

    @Test
    void getAllTeachersForSchool_delegatesCorrectSchoolToService() {
        Long schoolId = 3L;

        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
        when(lookupService.getAllTeachersForSchool(school)).thenReturn(List.of());

        controller.getAllTeachersForSchool(schoolId);

        verify(schoolRepository).findById(schoolId);
        verify(lookupService).getAllTeachersForSchool(school);
        verifyNoMoreInteractions(lookupService);
    }

    // -------------------------------------------------------------------------
    // getSchool (private helper — tested indirectly)
    // -------------------------------------------------------------------------

    @Test
    void getSchool_usesRepositoryFindById_withCorrectId() {
        Long schoolId = 42L;
        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
        when(lookupService.getUser(school, "u", Set.of(UserRole.STUDENT))).thenReturn(Map.of());

        controller.getUser(schoolId, "u", "student");

        verify(schoolRepository).findById(schoolId);
    }
}