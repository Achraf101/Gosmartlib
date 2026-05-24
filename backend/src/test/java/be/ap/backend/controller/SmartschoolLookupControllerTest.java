// package be.ap.backend.controller;

// import be.ap.backend.entity.School;
// import be.ap.backend.repository.SchoolRepository;
// import be.ap.backend.service.SmartschoolLookupService;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;

// import java.util.Map;
// import java.util.Optional;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.assertThatThrownBy;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class SmartschoolLookupControllerTest {

// @Mock
// private SmartschoolLookupService lookupService;

// @Mock
// private SchoolRepository schoolRepository;

// @InjectMocks
// private SmartschoolLookupController controller;

// private School school;

// @BeforeEach
// void setUp() {
// school = new School();
// // Assumes School has an id field; adjust setter name if generated
// differently.
// // school.setId(1L);
// }

// // -------------------------------------------------------------------------
// // getUser
// // -------------------------------------------------------------------------

// @Test
// void getUser_returnsOk_whenUserFound() {
// // Arrange
// Long schoolId = 1L;
// String ssId = "user-42";
// String role = "student";
// Map<String, Object> userData = Map.of("id", ssId, "role", role);

// when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
// when(lookupService.getUser(school, ssId, role)).thenReturn(userData);

// // Act
// ResponseEntity<Map<String, Object>> response = controller.getUser(schoolId,
// ssId, role);

// // Assert
// assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
// assertThat(response.getBody()).isEqualTo(userData);
// verify(lookupService).getUser(school, ssId, role);
// }

// @Test
// void getUser_returnsNotFound_whenServiceReturnsNull() {
// // Arrange
// Long schoolId = 1L;
// String ssId = "user-99";
// String role = "teacher";

// when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
// when(lookupService.getUser(school, ssId, role)).thenReturn(null);

// // Act
// ResponseEntity<Map<String, Object>> response = controller.getUser(schoolId,
// ssId, role);

// // Assert
// assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
// assertThat(response.getBody()).isNull();
// }

// @Test
// void getUser_throwsRuntimeException_whenSchoolNotFound() {
// // Arrange
// Long schoolId = 999L;
// when(schoolRepository.findById(schoolId)).thenReturn(Optional.empty());

// // Act & Assert
// assertThatThrownBy(() -> controller.getUser(schoolId, "any-ss-id",
// "student"))
// .isInstanceOf(RuntimeException.class)
// .hasMessageContaining("School not found: 999");

// verifyNoInteractions(lookupService);
// }

// @Test
// void getUser_passesRoleToService() {
// // Ensures the role parameter is forwarded without modification.
// Long schoolId = 1L;
// String ssId = "user-7";
// String role = "admin";

// when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
// when(lookupService.getUser(school, ssId, role)).thenReturn(Map.of("id",
// ssId));

// controller.getUser(schoolId, ssId, role);

// verify(lookupService).getUser(school, ssId, role);
// verifyNoMoreInteractions(lookupService);
// }

// // -------------------------------------------------------------------------
// // getClass (classroom lookup)
// // -------------------------------------------------------------------------

// @Test
// void getClass_returnsOk_whenClassroomFound() {
// // Arrange
// Long schoolId = 2L;
// String ssId = "class-A1";
// Map<String, Object> classData = Map.of("id", ssId, "name", "1A");

// when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
// when(lookupService.getClassroom(school, ssId)).thenReturn(classData);

// // Act
// ResponseEntity<Map<String, Object>> response = controller.getClass(schoolId,
// ssId);

// // Assert
// assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
// assertThat(response.getBody()).isEqualTo(classData);
// verify(lookupService).getClassroom(school, ssId);
// }

// @Test
// void getClass_returnsNotFound_whenServiceReturnsNull() {
// // Arrange
// Long schoolId = 2L;
// String ssId = "class-unknown";

// when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
// when(lookupService.getClassroom(school, ssId)).thenReturn(null);

// // Act
// ResponseEntity<Map<String, Object>> response = controller.getClass(schoolId,
// ssId);

// // Assert
// assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
// assertThat(response.getBody()).isNull();
// }

// @Test
// void getClass_throwsRuntimeException_whenSchoolNotFound() {
// // Arrange
// Long schoolId = 888L;
// when(schoolRepository.findById(schoolId)).thenReturn(Optional.empty());

// // Act & Assert
// assertThatThrownBy(() -> controller.getClass(schoolId, "class-X"))
// .isInstanceOf(RuntimeException.class)
// .hasMessageContaining("School not found: 888");

// verifyNoInteractions(lookupService);
// }

// @Test
// void getClass_delegatesCorrectSsIdToService() {
// // Ensures the ssId path variable is forwarded without mutation.
// Long schoolId = 2L;
// String ssId = "class-B2";

// when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
// when(lookupService.getClassroom(school, ssId)).thenReturn(Map.of("id",
// ssId));

// controller.getClass(schoolId, ssId);

// verify(lookupService).getClassroom(school, ssId);
// verifyNoMoreInteractions(lookupService);
// }

// // -------------------------------------------------------------------------
// // getSchool (private helper — tested indirectly via both endpoints)
// // -------------------------------------------------------------------------

// @Test
// void getSchool_usesRepositoryFindById_withCorrectId() {
// // Verify the correct schoolId is passed to the repository.
// Long schoolId = 42L;
// when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
// when(lookupService.getUser(school, "u", "student")).thenReturn(Map.of());

// controller.getUser(schoolId, "u", "student");

// verify(schoolRepository).findById(schoolId);
// }
// }