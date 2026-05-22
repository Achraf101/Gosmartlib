package be.ap.backend.service;

import be.ap.backend.entity.Classroom;
import be.ap.backend.entity.UserRole;
import be.ap.backend.repository.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private static final Long TEACHER_ID = 1L;
    private static final Long CLASSROOM_ID = 10L;
    private static final Long USER_ID = 2L;

    private Classroom classroom1;
    private Classroom classroom2;

    @BeforeEach
    void setUp() {
        classroom1 = new Classroom();
        classroom1.setId(CLASSROOM_ID);

        classroom2 = new Classroom();
        classroom2.setId(20L);
    }

    // --- getClassroomsForTeacher ---

    @Test
    void getClassroomsForTeacher_returnsClassrooms() {
        when(enrollmentRepository.findClassroomsByTeacherId(TEACHER_ID))
                .thenReturn(List.of(classroom1, classroom2));

        List<Classroom> result = enrollmentService.getClassroomsForTeacher(TEACHER_ID);

        assertThat(result).containsExactly(classroom1, classroom2);
        verify(enrollmentRepository).findClassroomsByTeacherId(TEACHER_ID);
    }

    @Test
    void getClassroomsForTeacher_returnsEmptyList_whenNoClassrooms() {
        when(enrollmentRepository.findClassroomsByTeacherId(TEACHER_ID))
                .thenReturn(List.of());

        List<Classroom> result = enrollmentService.getClassroomsForTeacher(TEACHER_ID);

        assertThat(result).isEmpty();
        verify(enrollmentRepository).findClassroomsByTeacherId(TEACHER_ID);
    }

    // --- isTeacherOfClassroom ---

    @Test
    void isTeacherOfClassroom_returnsTrue_whenTeacherIsEnrolled() {
        when(enrollmentRepository.existsByUserIdAndClassroomIdAndRole(
                TEACHER_ID, CLASSROOM_ID, UserRole.LEERKRACHT))
                .thenReturn(true);

        boolean result = enrollmentService.isTeacherOfClassroom(TEACHER_ID, CLASSROOM_ID);

        assertThat(result).isTrue();
        verify(enrollmentRepository).existsByUserIdAndClassroomIdAndRole(
                TEACHER_ID, CLASSROOM_ID, UserRole.LEERKRACHT);
    }

    @Test
    void isTeacherOfClassroom_returnsFalse_whenTeacherIsNotEnrolled() {
        when(enrollmentRepository.existsByUserIdAndClassroomIdAndRole(
                TEACHER_ID, CLASSROOM_ID, UserRole.LEERKRACHT))
                .thenReturn(false);

        boolean result = enrollmentService.isTeacherOfClassroom(TEACHER_ID, CLASSROOM_ID);

        assertThat(result).isFalse();
        verify(enrollmentRepository).existsByUserIdAndClassroomIdAndRole(
                TEACHER_ID, CLASSROOM_ID, UserRole.LEERKRACHT);
    }

    // --- isEnrolledInClassroom ---

    @Test
    void isEnrolledInClassroom_returnsTrue_whenUserIsEnrolled() {
        when(enrollmentRepository.existsByUserIdAndClassroomId(USER_ID, CLASSROOM_ID))
                .thenReturn(true);

        boolean result = enrollmentService.isEnrolledInClassroom(USER_ID, CLASSROOM_ID);

        assertThat(result).isTrue();
        verify(enrollmentRepository).existsByUserIdAndClassroomId(USER_ID, CLASSROOM_ID);
    }

    @Test
    void isEnrolledInClassroom_returnsFalse_whenUserIsNotEnrolled() {
        when(enrollmentRepository.existsByUserIdAndClassroomId(USER_ID, CLASSROOM_ID))
                .thenReturn(false);

        boolean result = enrollmentService.isEnrolledInClassroom(USER_ID, CLASSROOM_ID);

        assertThat(result).isFalse();
        verify(enrollmentRepository).existsByUserIdAndClassroomId(USER_ID, CLASSROOM_ID);
    }
}