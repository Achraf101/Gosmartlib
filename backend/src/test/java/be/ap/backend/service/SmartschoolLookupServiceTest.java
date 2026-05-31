package be.ap.backend.service;

import be.ap.backend.dto.TeacherDTO;
import be.ap.backend.entity.School;
import be.ap.backend.entity.User;
import be.ap.backend.entity.UserRole;
import be.ap.backend.repository.SchoolRepository;
import be.ap.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmartschoolLookupServiceTest {

    @Mock
    private SmartschoolTokenService tokenService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @InjectMocks
    private SmartschoolLookupService service;

    private static final Long SCHOOL_ID = 1L;

    private School school;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setSsSubdomain("testschool");
        school.setSsId("school-ss-id");
    }

    /**
     * Registers the default school stub. Call at the top of every test that uses
     * SCHOOL_ID.
     */
    private void stubSchool() {
        when(schoolRepository.findById(SCHOOL_ID)).thenReturn(Optional.of(school));
    }

    // -------------------------------------------------------------------------
    // getUser — endpoint selection
    // -------------------------------------------------------------------------

    @Test
    void getUser_student_usesStudentsEndpoint() {
        stubSchool();
        Map<String, Object> innerUser = Map.of("sourcedId", "s1", "givenName", "Alice");
        Map<String, Object> body = Map.of("user", innerUser);

        when(tokenService.getAccessToken(school)).thenReturn("tok-student");
        when(restTemplate.exchange(
                eq("https://testschool.smartschool.be/ims/oneroster/v1p1/students/s1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class))).thenReturn(ResponseEntity.ok(body));

        Map<String, Object> result = service.getUser(SCHOOL_ID, "s1", Set.of(UserRole.STUDENT));

        assertThat(result).isEqualTo(innerUser);
    }

    @Test
    void getUser_teacher_usesTeachersEndpoint() {
        stubSchool();
        Map<String, Object> innerUser = Map.of("sourcedId", "t1", "givenName", "Bob");
        Map<String, Object> body = Map.of("user", innerUser);

        when(tokenService.getAccessToken(school)).thenReturn("tok-teacher");
        when(restTemplate.exchange(
                eq("https://testschool.smartschool.be/ims/oneroster/v1p1/teachers/t1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class))).thenReturn(ResponseEntity.ok(body));

        Map<String, Object> result = service.getUser(SCHOOL_ID, "t1", Set.of(UserRole.LEERKRACHT));

        assertThat(result).isEqualTo(innerUser);
    }

    @Test
    void getUser_mixedRolesContainingStudent_usesStudentsEndpoint() {
        stubSchool();
        Map<String, Object> body = Map.of("user", Map.of("sourcedId", "s2"));

        when(tokenService.getAccessToken(school)).thenReturn("tok");
        when(restTemplate.exchange(
                eq("https://testschool.smartschool.be/ims/oneroster/v1p1/students/s2"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class))).thenReturn(ResponseEntity.ok(body));

        Map<String, Object> result = service.getUser(SCHOOL_ID, "s2",
                Set.of(UserRole.STUDENT, UserRole.LEERKRACHT));

        assertThat(result).isEqualTo(Map.of("sourcedId", "s2"));
    }

    // -------------------------------------------------------------------------
    // getUser — response handling
    // -------------------------------------------------------------------------

    @Test
    void getUser_noUserKey_returnsFlatBody() {
        stubSchool();
        Map<String, Object> body = Map.of("sourcedId", "s3", "givenName", "Carol");

        when(tokenService.getAccessToken(school)).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(body));

        Map<String, Object> result = service.getUser(SCHOOL_ID, "s3", Set.of(UserRole.STUDENT));

        assertThat(result).isEqualTo(body);
    }

    @Test
    void getUser_restTemplateThrows_returnsNull() {
        stubSchool();
        when(tokenService.getAccessToken(school)).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("connection refused"));

        Map<String, Object> result = service.getUser(SCHOOL_ID, "s4", Set.of(UserRole.STUDENT));

        assertThat(result).isNull();
    }

    @Test
    void getUser_nullBody_returnsNull() {
        stubSchool();
        when(tokenService.getAccessToken(school)).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(null));

        Map<String, Object> result = service.getUser(SCHOOL_ID, "s5", Set.of(UserRole.STUDENT));

        assertThat(result).isNull();
    }

    // -------------------------------------------------------------------------
    // getUser — school not found
    // -------------------------------------------------------------------------

    @Test
    void getUser_schoolNotFound_throwsEntityNotFoundException() {
        when(schoolRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUser(99L, "s1", Set.of(UserRole.STUDENT)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // getClassroom
    // -------------------------------------------------------------------------

    @Test
    void getClassroom_returnsClassBlock() {
        stubSchool();
        Map<String, Object> innerClass = Map.of("sourcedId", "cls1", "title", "Math 101");
        Map<String, Object> body = Map.of("class", innerClass);

        when(tokenService.getAccessToken(school)).thenReturn("tok");
        when(restTemplate.exchange(
                eq("https://testschool.smartschool.be/ims/oneroster/v1p1/classes/cls1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class))).thenReturn(ResponseEntity.ok(body));

        Map<String, Object> result = service.getClassroom(SCHOOL_ID, "cls1");

        assertThat(result).isEqualTo(innerClass);
    }

    @Test
    void getClassroom_noClassKey_returnsNull() {
        stubSchool();
        when(tokenService.getAccessToken(school)).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("something", "else")));

        Map<String, Object> result = service.getClassroom(SCHOOL_ID, "cls2");

        assertThat(result).isNull();
    }

    @Test
    void getClassroom_restTemplateThrows_returnsNull() {
        stubSchool();
        when(tokenService.getAccessToken(school)).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("timeout"));

        Map<String, Object> result = service.getClassroom(SCHOOL_ID, "cls3");

        assertThat(result).isNull();
    }

    @Test
    void getClassroom_nullBody_returnsNull() {
        stubSchool();
        when(tokenService.getAccessToken(school)).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(null));

        Map<String, Object> result = service.getClassroom(SCHOOL_ID, "cls4");

        assertThat(result).isNull();
    }

    @Test
    void getClassroom_schoolNotFound_throwsEntityNotFoundException() {
        when(schoolRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getClassroom(99L, "cls1"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // getAllTeachersForSchool
    // -------------------------------------------------------------------------

    @Test
    void getAllTeachersForSchool_returnsMappedDTOs() {
        stubSchool();
        Map<String, Object> u1 = Map.of("sourcedId", "t1", "givenName", "Alice", "familyName", "Smith");
        Map<String, Object> u2 = Map.of("sourcedId", "t2", "givenName", "Bob", "familyName", "Jones");
        Map<String, Object> body = Map.of("users", List.of(u1, u2));

        User dbUser1 = new User();
        dbUser1.setId(10L);
        User dbUser2 = new User();
        dbUser2.setId(20L);

        when(tokenService.getAccessToken(school)).thenReturn("tok");
        when(restTemplate.exchange(
                eq("https://testschool.smartschool.be/ims/oneroster/v1p1/schools/school-ss-id/teachers"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class))).thenReturn(ResponseEntity.ok(body));
        when(userRepository.findByOneRosterId("t1")).thenReturn(Optional.of(dbUser1));
        when(userRepository.findByOneRosterId("t2")).thenReturn(Optional.of(dbUser2));

        List<TeacherDTO> result = service.getAllTeachersForSchool(SCHOOL_ID);

        assertThat(result).containsExactly(
                new TeacherDTO(10L, "Alice", "Smith"),
                new TeacherDTO(20L, "Bob", "Jones"));
    }

    @Test
    void getAllTeachersForSchool_unknownUserIsSkipped() {
        stubSchool();
        Map<String, Object> u1 = Map.of("sourcedId", "t1", "givenName", "Alice", "familyName", "Smith");
        Map<String, Object> u2 = Map.of("sourcedId", "t-unknown", "givenName", "Ghost", "familyName", "User");
        Map<String, Object> body = Map.of("users", List.of(u1, u2));

        User dbUser1 = new User();
        dbUser1.setId(10L);

        when(tokenService.getAccessToken(school)).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(body));
        when(userRepository.findByOneRosterId("t1")).thenReturn(Optional.of(dbUser1));
        when(userRepository.findByOneRosterId("t-unknown")).thenReturn(Optional.empty());

        List<TeacherDTO> result = service.getAllTeachersForSchool(SCHOOL_ID);

        assertThat(result).containsExactly(new TeacherDTO(10L, "Alice", "Smith"));
    }

    @Test
    void getAllTeachersForSchool_emptyUsersList_returnsEmptyList() {
        stubSchool();
        when(tokenService.getAccessToken(school)).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("users", List.of())));

        List<TeacherDTO> result = service.getAllTeachersForSchool(SCHOOL_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void getAllTeachersForSchool_restTemplateThrows_returnsEmptyList() {
        stubSchool();
        when(tokenService.getAccessToken(school)).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("network error"));

        List<TeacherDTO> result = service.getAllTeachersForSchool(SCHOOL_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void getAllTeachersForSchool_nullBody_returnsEmptyList() {
        stubSchool();
        when(tokenService.getAccessToken(school)).thenReturn("tok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(null));

        List<TeacherDTO> result = service.getAllTeachersForSchool(SCHOOL_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void getAllTeachersForSchool_schoolNotFound_throwsEntityNotFoundException() {
        when(schoolRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAllTeachersForSchool(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // Request construction — Bearer token forwarded correctly
    // -------------------------------------------------------------------------

    @Test
    void getUser_setsAuthorizationHeaderWithToken() {
        stubSchool();
        when(tokenService.getAccessToken(school)).thenReturn("my-secret-token");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class)))
                .thenAnswer(invocation -> {
                    HttpEntity<?> entity = invocation.getArgument(2);
                    assertThat(entity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                            .isEqualTo("Bearer my-secret-token");
                    return ResponseEntity.ok(Map.of());
                });

        service.getUser(SCHOOL_ID, "s6", Set.of(UserRole.STUDENT));

        verify(tokenService).getAccessToken(school);
    }
}