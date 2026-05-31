package be.ap.backend.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import be.ap.backend.dto.ClassroomDTO;
import be.ap.backend.dto.StudentPreviewDTO;
import be.ap.backend.entity.Classroom;
import be.ap.backend.entity.Loan;
import be.ap.backend.entity.User;
import be.ap.backend.repository.ClassroomRepository;
import be.ap.backend.repository.LoanRepository;
import jakarta.persistence.EntityNotFoundException;

/**
 * Service for classroom-related operations available to teachers.
 */
@Service
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final LoanRepository loanRepository;
    private final SmartschoolLookupService lookupService;
    private final EnrollmentService enrollmentService;

    public ClassroomService(ClassroomRepository classroomRepository, LoanRepository loanRepository,
            SmartschoolLookupService lookupService, EnrollmentService enrollmentService) {
        this.classroomRepository = classroomRepository;
        this.loanRepository = loanRepository;
        this.lookupService = lookupService;
        this.enrollmentService = enrollmentService;
    }

    /**
     * Returns all classrooms for the given teacher, with names resolved from
     * Smartschool.
     */
    public List<ClassroomDTO> getClassroomsForTeacher(Long teacherId) {

        return enrollmentService.getClassroomsForTeacher(teacherId).stream()
                .map(c -> {
                    Map<String, Object> classData = lookupService.getClassroom(c.getSchool().getId(), c.getSsId());
                    String name = classData != null ? (String) classData.get("title") : c.getName();
                    return new ClassroomDTO(c.getId(), name, c.getStudents().size());
                })
                .toList();
    }

    /**
     * Returns all students in the given classroom, sorted by last name then first
     * name.
     *
     * @throws EntityNotFoundException if the classroom does not exist
     * @throws SecurityException       if the given teacher is not enrolled in the
     *                                 classroom
     */
    public List<StudentPreviewDTO> getStudentsForClassroom(Long teacherId, Long classroomId) {
        Classroom classroom = classroomRepository.findByIdWithStudents(classroomId)
                .orElseThrow(() -> new EntityNotFoundException("Klas niet gevonden met id: " + classroomId));

        if (!enrollmentService.isTeacherOfClassroom(teacherId, classroomId)) {
            throw new SecurityException("Je bent niet de leerkracht van deze klas.");
        }

        return classroom.getStudents().stream()
                .map(this::toStudentPreview)
                .sorted(Comparator
                        .comparing(
                                (StudentPreviewDTO p) -> p.getLastName() != null ? p.getLastName().toLowerCase() : "")
                        .thenComparing(p -> p.getFirstName() != null ? p.getFirstName().toLowerCase() : ""))
                .toList();
    }

    /**
     * Returns {@code true} if the given teacher and student share at least one
     * classroom.
     */
    public boolean teacherCanViewStudent(Long teacherId, Long studentId) {
        return classroomRepository.teacherHasStudent(teacherId, studentId);
    }

    private StudentPreviewDTO toStudentPreview(User student) {
        Optional<LocalDate> last = loanRepository.findByUserId(student.getId()).stream()
                .map(Loan::getStart)
                .filter(d -> d != null)
                .max(Comparator.naturalOrder());

        Map<String, Object> user = lookupService.getUser(student.getSchool().getId(), student.getOneRosterId(),
                student.getRoles());

        String firstName = (String) user.get("givenName");
        String lastName = (String) user.get("familyName");

        return new StudentPreviewDTO(
                student.getId(),
                firstName,
                lastName,
                last.orElse(null));
    }
}