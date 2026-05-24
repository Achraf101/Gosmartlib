package be.ap.backend.service;

import be.ap.backend.entity.*;
import be.ap.backend.repository.ClassroomRepository;
import be.ap.backend.repository.EnrollmentRepository;
import be.ap.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmartschoolSyncServiceTest {

    @Mock
    private SmartschoolTokenService tokenService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ClassroomRepository classroomRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SmartschoolSyncService syncService;

    private School school;
    private static final String SUBDOMAIN = "testschool";
    private static final String ACCESS_TOKEN = "test-token";

    @BeforeEach
    void setUp() {
        school = new School();
        school.setSsSubdomain(SUBDOMAIN);

        ReflectionTestUtils.setField(syncService, "scope", "test-scope");

        lenient().when(tokenService.getAccessToken(school)).thenReturn(ACCESS_TOKEN);
    }

    // -------------------------------------------------------------------------
    // syncSchool — happy path
    // -------------------------------------------------------------------------

    @Test
    void syncSchool_callsAllThreeSubSyncs() {
        stubEmptyPage("students", "users");
        stubEmptyPage("teachers", "users");
        stubEmptyPage("classes", "classes");
        stubEmptyPage("enrollments", "enrollments");

        syncService.syncSchool(school);

        verify(restTemplate, atLeast(4)).exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class));
    }

    @Test
    void syncSchool_completesNormallyWhenAllRequestsFail() {
        // get() catches all HTTP exceptions and returns null; callers treat null as
        // "no more pages" and break, so syncSchool never throws.
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("network error"));

        assertThatNoException().isThrownBy(() -> syncService.syncSchool(school));
    }

    // -------------------------------------------------------------------------
    // syncUsers — students
    // -------------------------------------------------------------------------

    @Test
    void syncUsers_createsNewStudentWhenNotInRepository() {
        Map<String, Object> user = buildUserPayload("sor-1", "legacy-1");
        stubSinglePage("students", "users", List.of(user));
        stubEmptyPage("teachers", "users");
        stubEmptyPage("classes", "classes");
        stubEmptyPage("enrollments", "enrollments");

        when(userRepository.findBySsId("legacy-1")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        syncService.syncSchool(school);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, atLeastOnce()).save(captor.capture());

        User saved = captor.getAllValues().stream()
                .filter(u -> "sor-1".equals(u.getOneRosterId()))
                .findFirst()
                .orElseThrow();

        assertThat(saved.getRoles()).isEqualTo(new HashSet<>(Set.of(UserRole.STUDENT)));
        assertThat(saved.getSsId()).isEqualTo("legacy-1");
        assertThat(saved.getSchool()).isEqualTo(school);
    }

    @Test
    void syncUsers_updatesExistingStudentByLegacyId() {
        User existing = new User();
        existing.setSsId("legacy-1");

        Map<String, Object> user = buildUserPayload("sor-new", "legacy-1");
        stubSinglePage("students", "users", List.of(user));
        stubEmptyPage("teachers", "users");
        stubEmptyPage("classes", "classes");
        stubEmptyPage("enrollments", "enrollments");

        when(userRepository.findBySsId("legacy-1")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        syncService.syncSchool(school);

        verify(userRepository, atLeastOnce()).save(existing);
        assertThat(existing.getOneRosterId()).isEqualTo("sor-new");
    }

    @Test
    void syncUsers_skipsUserWithNullSourcedId() {
        Map<String, Object> user = new HashMap<>();
        user.put("sourcedId", null);

        stubSinglePage("students", "users", List.of(user));
        stubEmptyPage("teachers", "users");
        stubEmptyPage("classes", "classes");
        stubEmptyPage("enrollments", "enrollments");

        syncService.syncSchool(school);

        verify(userRepository, never()).save(any());
    }

    @Test
    void syncUsers_handlesNullMetadata() {
        Map<String, Object> user = new HashMap<>();
        user.put("sourcedId", "sor-1");
        user.put("metadata", null);

        stubSinglePage("students", "users", List.of(user));
        stubEmptyPage("teachers", "users");
        stubEmptyPage("classes", "classes");
        stubEmptyPage("enrollments", "enrollments");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        syncService.syncSchool(school);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, atLeastOnce()).save(captor.capture());

        User saved = captor.getAllValues().stream()
                .filter(u -> "sor-1".equals(u.getOneRosterId()))
                .findFirst()
                .orElseThrow();
        assertThat(saved.getSsId()).isNull();
    }

    @Test
    void syncUsers_syncsTeachersWithCorrectRole() {
        Map<String, Object> teacher = buildUserPayload("sor-t1", "legacy-t1");
        stubEmptyPage("students", "users");
        stubSinglePage("teachers", "users", List.of(teacher));
        stubEmptyPage("classes", "classes");
        stubEmptyPage("enrollments", "enrollments");

        when(userRepository.findBySsId("legacy-t1")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        syncService.syncSchool(school);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, atLeastOnce()).save(captor.capture());

        User saved = captor.getAllValues().stream()
                .filter(u -> "sor-t1".equals(u.getOneRosterId()))
                .findFirst()
                .orElseThrow();
        assertThat(saved.getRoles()).isEqualTo(new HashSet<>(Set.of(UserRole.LEERKRACHT)));
    }

    @Test
    void syncUsers_paginatesUntilPageSmallerThanPageSize() {
        // Page 1: full page (100 items), page 2: partial page (1 item)
        List<Map<String, Object>> fullPage = buildUserList(100);
        List<Map<String, Object>> lastPage = List.of(buildUserPayload("sor-last", "legacy-last"));

        when(restTemplate.exchange(
                contains("students"), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("users", fullPage)))
                .thenReturn(ResponseEntity.ok(Map.of("users", lastPage)));

        stubEmptyPage("teachers", "users");
        stubEmptyPage("classes", "classes");
        stubEmptyPage("enrollments", "enrollments");

        when(userRepository.findBySsId(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        syncService.syncSchool(school);

        // 2 pages × students endpoint
        verify(restTemplate, atLeast(2)).exchange(contains("students"), eq(HttpMethod.GET), any(), eq(Map.class));
    }

    // -------------------------------------------------------------------------
    // syncClassrooms
    // -------------------------------------------------------------------------

    @Test
    void syncClassrooms_createsNewClassroom() {
        Map<String, Object> cls = Map.of("sourcedId", "class-1");
        stubEmptyPage("students", "users");
        stubEmptyPage("teachers", "users");
        stubSinglePage("classes", "classes", List.of(cls));
        stubEmptyPage("enrollments", "enrollments");

        when(classroomRepository.findBySsId("class-1")).thenReturn(Optional.empty());
        when(classroomRepository.save(any(Classroom.class))).thenAnswer(inv -> inv.getArgument(0));

        syncService.syncSchool(school);

        ArgumentCaptor<Classroom> captor = ArgumentCaptor.forClass(Classroom.class);
        verify(classroomRepository, atLeastOnce()).save(captor.capture());

        Classroom saved = captor.getAllValues().stream()
                .filter(c -> "class-1".equals(c.getSsId()))
                .findFirst()
                .orElseThrow();

        assertThat(saved.getSchool()).isEqualTo(school);
    }

    @Test
    void syncClassrooms_skipsClassroomWithNullSourcedId() {
        Map<String, Object> cls = new HashMap<>();
        cls.put("sourcedId", null);

        stubEmptyPage("students", "users");
        stubEmptyPage("teachers", "users");
        stubSinglePage("classes", "classes", List.of(cls));
        stubEmptyPage("enrollments", "enrollments");

        syncService.syncSchool(school);

        verify(classroomRepository, never()).save(any());
    }

    @Test
    void syncClassrooms_updatesExistingClassroom() {
        Classroom existing = new Classroom();
        existing.setSsId("class-1");

        Map<String, Object> cls = Map.of("sourcedId", "class-1");
        stubEmptyPage("students", "users");
        stubEmptyPage("teachers", "users");
        stubSinglePage("classes", "classes", List.of(cls));
        stubEmptyPage("enrollments", "enrollments");

        when(classroomRepository.findBySsId("class-1")).thenReturn(Optional.of(existing));
        when(classroomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        syncService.syncSchool(school);

        verify(classroomRepository).save(existing);
    }

    // -------------------------------------------------------------------------
    // syncEnrollments
    // -------------------------------------------------------------------------

    @Test
    void syncEnrollments_createsEnrollmentForStudentRole() {
        User user = new User();
        Classroom classroom = new Classroom();

        Map<String, Object> enrollment = buildEnrollmentPayload("enr-1", "sor-u1", "class-1", "student");
        stubEmptyPage("students", "users");
        stubEmptyPage("teachers", "users");
        stubEmptyPage("classes", "classes");
        stubSinglePage("enrollments", "enrollments", List.of(enrollment));

        when(userRepository.findByOneRosterId("sor-u1")).thenReturn(Optional.of(user));
        when(classroomRepository.findBySsId("class-1")).thenReturn(Optional.of(classroom));
        when(enrollmentRepository.findByOneRosterId("enr-1")).thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        syncService.syncSchool(school);

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository, atLeastOnce()).save(captor.capture());

        Enrollment saved = captor.getAllValues().stream()
                .filter(e -> "enr-1".equals(e.getOneRosterId()))
                .findFirst()
                .orElseThrow();

        assertThat(saved.getRole()).isEqualTo(UserRole.STUDENT);
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getClassroom()).isEqualTo(classroom);
        assertThat(saved.getSchool()).isEqualTo(school);
    }

    @Test
    void syncEnrollments_setsTeacherRole() {
        Map<String, Object> enrollment = buildEnrollmentPayload("enr-2", "sor-t1", "class-1", "teacher");
        stubEmptyPage("students", "users");
        stubEmptyPage("teachers", "users");
        stubEmptyPage("classes", "classes");
        stubSinglePage("enrollments", "enrollments", List.of(enrollment));

        when(userRepository.findByOneRosterId("sor-t1")).thenReturn(Optional.empty());
        when(classroomRepository.findBySsId("class-1")).thenReturn(Optional.empty());
        when(enrollmentRepository.findByOneRosterId("enr-2")).thenReturn(Optional.empty());
        when(enrollmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        syncService.syncSchool(school);

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository, atLeastOnce()).save(captor.capture());

        Enrollment saved = captor.getAllValues().stream()
                .filter(e -> "enr-2".equals(e.getOneRosterId()))
                .findFirst()
                .orElseThrow();

        assertThat(saved.getRole()).isEqualTo(UserRole.LEERKRACHT);
    }

    @Test
    void syncEnrollments_setsNullRoleForUnknownRoleString() {
        Map<String, Object> enrollment = buildEnrollmentPayload("enr-3", "sor-u1", "class-1", "administrator");
        stubEmptyPage("students", "users");
        stubEmptyPage("teachers", "users");
        stubEmptyPage("classes", "classes");
        stubSinglePage("enrollments", "enrollments", List.of(enrollment));

        when(userRepository.findByOneRosterId(anyString())).thenReturn(Optional.empty());
        when(classroomRepository.findBySsId(anyString())).thenReturn(Optional.empty());
        when(enrollmentRepository.findByOneRosterId("enr-3")).thenReturn(Optional.empty());
        when(enrollmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        syncService.syncSchool(school);

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository, atLeastOnce()).save(captor.capture());

        Enrollment saved = captor.getAllValues().stream()
                .filter(e -> "enr-3".equals(e.getOneRosterId()))
                .findFirst()
                .orElseThrow();

        assertThat(saved.getRole()).isNull();
    }

    @Test
    void syncEnrollments_skipsEnrollmentWithNullSourcedId() {
        Map<String, Object> enrollment = new HashMap<>();
        enrollment.put("sourcedId", null);

        stubEmptyPage("students", "users");
        stubEmptyPage("teachers", "users");
        stubEmptyPage("classes", "classes");
        stubSinglePage("enrollments", "enrollments", List.of(enrollment));

        syncService.syncSchool(school);

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void syncEnrollments_handlesNullUserAndClassroomRefs() {
        Map<String, Object> enrollment = new HashMap<>();
        enrollment.put("sourcedId", "enr-null");
        enrollment.put("user", null);
        enrollment.put("class", null);
        enrollment.put("role", "student");

        stubEmptyPage("students", "users");
        stubEmptyPage("teachers", "users");
        stubEmptyPage("classes", "classes");
        stubSinglePage("enrollments", "enrollments", List.of(enrollment));

        when(enrollmentRepository.findByOneRosterId("enr-null")).thenReturn(Optional.empty());
        when(enrollmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        syncService.syncSchool(school);

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository, atLeastOnce()).save(captor.capture());

        Enrollment saved = captor.getValue();
        assertThat(saved.getUser()).isNull();
        assertThat(saved.getClassroom()).isNull();
    }

    // -------------------------------------------------------------------------
    // HTTP layer
    // -------------------------------------------------------------------------

    @Test
    void get_returnsNullOnRestTemplateException_andSyncCompletesWithoutThrowing() {
        // get() catches all exceptions internally and returns null.
        // The callers treat null as "no more pages" and break, so syncSchool finishes
        // normally.
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("timeout"));

        assertThatNoException().isThrownBy(() -> syncService.syncSchool(school));

        // Nothing was persisted because every GET returned null
        verify(userRepository, never()).save(any());
        verify(classroomRepository, never()).save(any());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void get_sendsBearerTokenHeader() {
        stubEmptyPage("students", "users");
        stubEmptyPage("teachers", "users");
        stubEmptyPage("classes", "classes");
        stubEmptyPage("enrollments", "enrollments");

        syncService.syncSchool(school);

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate, atLeastOnce())
                .exchange(anyString(), eq(HttpMethod.GET), entityCaptor.capture(), eq(Map.class));

        HttpHeaders headers = entityCaptor.getValue().getHeaders();
        assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION))
                .isEqualTo("Bearer " + ACCESS_TOKEN);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Map<String, Object> buildUserPayload(String oneRosterId, String legacyId) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("smsc.legacyIdentifier", legacyId);

        Map<String, Object> user = new HashMap<>();
        user.put("sourcedId", oneRosterId);
        user.put("metadata", metadata);
        return user;
    }

    private Map<String, Object> buildEnrollmentPayload(String id, String userSorId, String classSorId, String role) {
        Map<String, Object> enrollment = new HashMap<>();
        enrollment.put("sourcedId", id);
        enrollment.put("user", Map.of("sourcedId", userSorId));
        enrollment.put("class", Map.of("sourcedId", classSorId));
        enrollment.put("role", role);
        return enrollment;
    }

    private List<Map<String, Object>> buildUserList(int count) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(buildUserPayload("sor-" + i, "legacy-" + i));
        }
        return list;
    }

    /** Stub a single-page response (fewer items than PAGE_SIZE → loop stops). */
    private void stubSinglePage(String urlFragment, String responseKey, List<Map<String, Object>> items) {
        when(restTemplate.exchange(
                contains(urlFragment), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of(responseKey, items)));
    }

    /** Stub an empty first page so the loop exits immediately. */
    private void stubEmptyPage(String urlFragment, String responseKey) {
        when(restTemplate.exchange(
                contains(urlFragment), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of(responseKey, List.of())));
    }
}