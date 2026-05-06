package be.ap.backend.service;

import be.ap.backend.dto.ClassroomDTO;
import be.ap.backend.dto.StudentPreviewDTO;
import be.ap.backend.entity.Classroom;
import be.ap.backend.entity.Loan;
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

    @InjectMocks
    private ClassroomService classroomService;

    private User teacher;
    private User student1;
    private User student2;
    private Classroom classroom;

    @BeforeEach
    void setUp() {
        teacher = new User();
        teacher.setId(1L);
        teacher.setUsername("teacher1");
        teacher.setRole(UserRole.LEERKRACHT);

        student1 = new User();
        student1.setId(2L);
        student1.setUsername("anna");
        student1.setRole(UserRole.STUDENT);

        student2 = new User();
        student2.setId(3L);
        student2.setUsername("ben");
        student2.setRole(UserRole.STUDENT);

        classroom = new Classroom();
        classroom.setId(10L);
        classroom.setName("3A");
        classroom.setTeacher(teacher);
        classroom.setStudents(new HashSet<>(Set.of(student1, student2)));
    }

    // ── getClassroomsForTeacher ───────────────────────────────────

    @Test
    void getClassroomsForTeacher_returnsListOfDTOs() {
        when(classroomRepository.findByTeacherIdWithStudents(1L)).thenReturn(List.of(classroom));

        List<ClassroomDTO> result = classroomService.getClassroomsForTeacher(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("3A");
        assertThat(result.get(0).getStudentCount()).isEqualTo(2);
    }

    @Test
    void getClassroomsForTeacher_empty_returnsEmptyList() {
        when(classroomRepository.findByTeacherIdWithStudents(1L)).thenReturn(List.of());

        List<ClassroomDTO> result = classroomService.getClassroomsForTeacher(1L);

        assertThat(result).isEmpty();
    }

    // ── getStudentsForClassroom ───────────────────────────────────

    @Test
    void getStudentsForClassroom_success_returnsStudents() {
        when(classroomRepository.findByIdWithStudents(10L)).thenReturn(Optional.of(classroom));
        when(loanRepository.findByUserId(2L)).thenReturn(List.of());
        when(loanRepository.findByUserId(3L)).thenReturn(List.of());

        List<StudentPreviewDTO> result = classroomService.getStudentsForClassroom(1L, 10L);

        assertThat(result).hasSize(2);
    }

    @Test
    void getStudentsForClassroom_sortedByName() {
        when(classroomRepository.findByIdWithStudents(10L)).thenReturn(Optional.of(classroom));
        when(loanRepository.findByUserId(2L)).thenReturn(List.of());
        when(loanRepository.findByUserId(3L)).thenReturn(List.of());

        List<StudentPreviewDTO> result = classroomService.getStudentsForClassroom(1L, 10L);

        List<String> names = result.stream().map(StudentPreviewDTO::getName).toList();
        assertThat(names).isSorted();
    }

    @Test
    void getStudentsForClassroom_lastActivityFromLatestLoan() {
        Loan loan = new Loan();
        loan.setStart(LocalDate.of(2025, 3, 10));
        when(classroomRepository.findByIdWithStudents(10L)).thenReturn(Optional.of(classroom));
        when(loanRepository.findByUserId(2L)).thenReturn(List.of(loan));
        when(loanRepository.findByUserId(3L)).thenReturn(List.of());

        List<StudentPreviewDTO> result = classroomService.getStudentsForClassroom(1L, 10L);

        StudentPreviewDTO anna = result.stream().filter(s -> s.getUsername().equals("anna")).findFirst().orElseThrow();
        assertThat(anna.getLastActivity()).isEqualTo(LocalDate.of(2025, 3, 10));
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
