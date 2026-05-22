package be.ap.backend.service;

import be.ap.backend.entity.Classroom;
import be.ap.backend.entity.UserRole;
import be.ap.backend.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    public List<Classroom> getClassroomsForTeacher(Long teacherId) {
        return enrollmentRepository.findClassroomsByTeacherId(teacherId);
    }

    public boolean isTeacherOfClassroom(Long teacherId, Long classroomId) {
        return enrollmentRepository.existsByUserIdAndClassroomIdAndRole(
                teacherId, classroomId, UserRole.LEERKRACHT);
    }

    public boolean isEnrolledInClassroom(Long userId, Long classroomId) {
        return enrollmentRepository.existsByUserIdAndClassroomId(userId, classroomId);
    }
}