package be.ap.backend.service;

import be.ap.backend.dto.ClassroomDTO;
import be.ap.backend.dto.StudentPreviewDTO;
import be.ap.backend.entity.Classroom;
import be.ap.backend.entity.Enrollment;
import be.ap.backend.entity.Loan;
import be.ap.backend.entity.School;
import be.ap.backend.entity.User;
import be.ap.backend.entity.UserRole;
import be.ap.backend.repository.ClassroomRepository;
import be.ap.backend.repository.LoanRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClassroomServiceTest {

    @Mock
    private ClassroomRepository classroomRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private SmartschoolLookupService lookupService;

    @Mock
    private EnrollmentService enrollmentService;

    @InjectMocks
    private ClassroomService classroomService;

    // Shared school — needed because the service calls getSchool().getId()
    private School school;

    private User student1;
    private User student2;
    private Classroom classroom;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1L);

        student1 = new User();
        student1.setId(2L);
        student1.setUsername("anna");
        student1.setRoles(new HashSet<>(Set.of(UserRole.STUDENT)));
        student1.setOneRosterId("sis-001");
        student1.setSchool(school);

        student2 = new User();
        student2.setId(3L);
        student2.setUsername("ben");
        student2.setRoles(new HashSet<>(Set.of(UserRole.STUDENT)));
        student2.setOneRosterId("sis-002");
        student2.setSchool(school);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setUser(student1);
        enrollment1.setRole(UserRole.STUDENT);

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setUser(student2);
        enrollment2.setRole(UserRole.STUDENT);

        classroom = new Classroom();
        classroom.setId(10L);
        classroom.setName("3A");
        classroom.setSchool(school);
        classroom.getEnrollments().add(enrollment1);
        classroom.getEnrollments().add(enrollment2);
    }

    // ── getClassroomsForTeacher ───────────────────────────────────

    @Test
    void getClassroomsForTeacher_returnsListOfDTOs() {
        when(enrollmentService.getClassroomsForTeacher(1L)).thenReturn(List.of(classroom));
        when(lookupService.getClassroom(school.getId(), classroom.getSsId()))
                .thenReturn(Map.of("title", "3A"));

        List<ClassroomDTO> result = classroomService.getClassroomsForTeacher(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("3A");
        assertThat(result.get(0).getStudentCount()).isEqualTo(2);
    }

    @Test
    void getClassroomsForTeacher_lookupReturnsNull_fallsBackToEntityName() {
        when(enrollmentService.getClassroomsForTeacher(1L)).thenReturn(List.of(classroom));
        when(lookupService.getClassroom(school.getId(), classroom.getSsId()))
                .thenReturn(null);

        List<ClassroomDTO> result = classroomService.getClassroomsForTeacher(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("3A");
    }

    @Test
    void getClassroomsForTeacher_empty_returnsEmptyList() {
        when(enrollmentService.getClassroomsForTeacher(1L)).thenReturn(List.of());

        List<ClassroomDTO> result = classroomService.getClassroomsForTeacher(1L);

        assertThat(result).isEmpty();
    }

    // ── getStudentsForClassroom ───────────────────────────────────

    @Test
    void getStudentsForClassroom_success_returnsStudents() {
        when(classroomRepository.findByIdWithStudents(10L)).thenReturn(Optional.of(classroom));
        when(enrollmentService.isTeacherOfClassroom(1L, 10L)).thenReturn(true);
        when(loanRepository.findByUserId(2L)).thenReturn(List.of());
        when(loanRepository.findByUserId(3L)).thenReturn(List.of());
        when(lookupService.getUser(school.getId(), student1.getOneRosterId(), student1.getRoles()))
                .thenReturn(Map.of("givenName", "Anna", "familyName", "Aerts"));
        when(lookupService.getUser(school.getId(), student2.getOneRosterId(), student2.getRoles()))
                .thenReturn(Map.of("givenName", "Ben", "familyName", "Bogaert"));

        List<StudentPreviewDTO> result = classroomService.getStudentsForClassroom(1L, 10L);

        assertThat(result).hasSize(2);
    }

    @Test
    void getStudentsForClassroom_sortedByLastNameThenFirstName() {
        when(classroomRepository.findByIdWithStudents(10L)).thenReturn(Optional.of(classroom));
        when(enrollmentService.isTeacherOfClassroom(1L, 10L)).thenReturn(true);
        when(loanRepository.findByUserId(2L)).thenReturn(List.of());
        when(loanRepository.findByUserId(3L)).thenReturn(List.of());
        when(lookupService.getUser(school.getId(), student1.getOneRosterId(), student1.getRoles()))
                .thenReturn(Map.of("givenName", "Anna", "familyName", "Bogaert"));
        when(lookupService.getUser(school.getId(), student2.getOneRosterId(), student2.getRoles()))
                .thenReturn(Map.of("givenName", "Ben", "familyName", "Aerts"));

        List<StudentPreviewDTO> result = classroomService.getStudentsForClassroom(1L, 10L);

        assertThat(result.stream().map(StudentPreviewDTO::getLastName).toList())
                .containsExactly("Aerts", "Bogaert");
    }

    @Test
    void getStudentsForClassroom_sortedByFirstNameWhenLastNameEqual() {
        User student3 = new User();
        student3.setId(4L);
        student3.setRoles(new HashSet<>(Set.of(UserRole.STUDENT)));
        student3.setOneRosterId("sis-003");
        student3.setSchool(school);

        Enrollment enrollment3 = new Enrollment();
        enrollment3.setUser(student3);
        enrollment3.setRole(UserRole.STUDENT);
        classroom.getEnrollments().add(enrollment3);

        when(classroomRepository.findByIdWithStudents(10L)).thenReturn(Optional.of(classroom));
        when(enrollmentService.isTeacherOfClassroom(1L, 10L)).thenReturn(true);
        when(loanRepository.findByUserId(2L)).thenReturn(List.of());
        when(loanRepository.findByUserId(3L)).thenReturn(List.of());
        when(loanRepository.findByUserId(4L)).thenReturn(List.of());
        when(lookupService.getUser(school.getId(), student1.getOneRosterId(), student1.getRoles()))
                .thenReturn(Map.of("givenName", "Zara", "familyName", "Aerts"));
        when(lookupService.getUser(school.getId(), student2.getOneRosterId(), student2.getRoles()))
                .thenReturn(Map.of("givenName", "Ben", "familyName", "Bogaert"));
        when(lookupService.getUser(school.getId(), student3.getOneRosterId(), student3.getRoles()))
                .thenReturn(Map.of("givenName", "Anna", "familyName", "Aerts"));

        List<StudentPreviewDTO> result = classroomService.getStudentsForClassroom(1L, 10L);

        assertThat(result.stream().map(StudentPreviewDTO::getFirstName).toList())
                .containsExactly("Anna", "Zara", "Ben");
    }

    @Test
    void getStudentsForClassroom_lastActivityFromLatestLoan() {
        Loan loan = new Loan();
        loan.setStart(LocalDate.of(2025, 3, 10));

        when(classroomRepository.findByIdWithStudents(10L)).thenReturn(Optional.of(classroom));
        when(enrollmentService.isTeacherOfClassroom(1L, 10L)).thenReturn(true);
        when(loanRepository.findByUserId(2L)).thenReturn(List.of(loan));
        when(loanRepository.findByUserId(3L)).thenReturn(List.of());
        when(lookupService.getUser(school.getId(), student1.getOneRosterId(), student1.getRoles()))
                .thenReturn(Map.of("givenName", "Anna", "familyName", "Aerts"));
        when(lookupService.getUser(school.getId(), student2.getOneRosterId(), student2.getRoles()))
                .thenReturn(Map.of("givenName", "Ben", "familyName", "Bogaert"));

        List<StudentPreviewDTO> result = classroomService.getStudentsForClassroom(1L, 10L);

        StudentPreviewDTO anna = result.stream()
                .filter(s -> "Anna".equals(s.getFirstName()))
                .findFirst().orElseThrow();
        assertThat(anna.getLastActivity()).isEqualTo(LocalDate.of(2025, 3, 10));
    }

    @Test
    void getStudentsForClassroom_multipleLoans_picksLatest() {
        Loan older = new Loan();
        older.setStart(LocalDate.of(2024, 9, 1));

        Loan newer = new Loan();
        newer.setStart(LocalDate.of(2025, 6, 15));

        Loan middle = new Loan();
        middle.setStart(LocalDate.of(2025, 1, 20));

        when(classroomRepository.findByIdWithStudents(10L)).thenReturn(Optional.of(classroom));
        when(enrollmentService.isTeacherOfClassroom(1L, 10L)).thenReturn(true);
        when(loanRepository.findByUserId(2L)).thenReturn(List.of(older, newer, middle));
        when(loanRepository.findByUserId(3L)).thenReturn(List.of());
        when(lookupService.getUser(school.getId(), student1.getOneRosterId(), student1.getRoles()))
                .thenReturn(Map.of("givenName", "Anna", "familyName", "Aerts"));
        when(lookupService.getUser(school.getId(), student2.getOneRosterId(), student2.getRoles()))
                .thenReturn(Map.of("givenName", "Ben", "familyName", "Bogaert"));

        List<StudentPreviewDTO> result = classroomService.getStudentsForClassroom(1L, 10L);

        StudentPreviewDTO anna = result.stream()
                .filter(s -> "Anna".equals(s.getFirstName()))
                .findFirst().orElseThrow();
        assertThat(anna.getLastActivity()).isEqualTo(LocalDate.of(2025, 6, 15));
    }

    @Test
    void getStudentsForClassroom_noLoans_lastActivityIsNull() {
        when(classroomRepository.findByIdWithStudents(10L)).thenReturn(Optional.of(classroom));
        when(enrollmentService.isTeacherOfClassroom(1L, 10L)).thenReturn(true);
        when(loanRepository.findByUserId(2L)).thenReturn(List.of());
        when(loanRepository.findByUserId(3L)).thenReturn(List.of());
        when(lookupService.getUser(school.getId(), student1.getOneRosterId(), student1.getRoles()))
                .thenReturn(Map.of("givenName", "Anna", "familyName", "Aerts"));
        when(lookupService.getUser(school.getId(), student2.getOneRosterId(), student2.getRoles()))
                .thenReturn(Map.of("givenName", "Ben", "familyName", "Bogaert"));

        List<StudentPreviewDTO> result = classroomService.getStudentsForClassroom(1L, 10L);

        assertThat(result).allSatisfy(s -> assertThat(s.getLastActivity()).isNull());
    }

    @Test
    void getStudentsForClassroom_classroomNotFound_throwsEntityNotFoundException() {
        when(classroomRepository.findByIdWithStudents(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> classroomService.getStudentsForClassroom(1L, 99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Klas niet gevonden");
    }

    @Test
    void getStudentsForClassroom_wrongTeacher_throwsSecurityException() {
        when(classroomRepository.findByIdWithStudents(10L)).thenReturn(Optional.of(classroom));
        when(enrollmentService.isTeacherOfClassroom(99L, 10L)).thenReturn(false);

        assertThatThrownBy(() -> classroomService.getStudentsForClassroom(99L, 10L))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("niet de leerkracht");
    }

    // ── teacherCanViewStudent ─────────────────────────────────────

    @Test
    void teacherCanViewStudent_returnsTrue() {
        when(classroomRepository.teacherHasStudent(1L, 2L)).thenReturn(true);

        assertThat(classroomService.teacherCanViewStudent(1L, 2L)).isTrue();
    }

    @Test
    void teacherCanViewStudent_returnsFalse() {
        when(classroomRepository.teacherHasStudent(1L, 99L)).thenReturn(false);

        assertThat(classroomService.teacherCanViewStudent(1L, 99L)).isFalse();
    }
}