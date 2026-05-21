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

@Service
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final LoanRepository loanRepository;
    private final SmartschoolLookupService lookupService;

    public ClassroomService(ClassroomRepository classroomRepository, LoanRepository loanRepository,
            SmartschoolLookupService lookupService) {
        this.classroomRepository = classroomRepository;
        this.loanRepository = loanRepository;
        this.lookupService = lookupService;
    }

    public List<ClassroomDTO> getClassroomsForTeacher(Long teacherId) {
        return classroomRepository.findByTeacherIdWithStudents(teacherId).stream()
                .map(c -> new ClassroomDTO(c.getId(), c.getName(), c.getStudents().size()))
                .toList();
    }

    public List<StudentPreviewDTO> getStudentsForClassroom(Long teacherId, Long classroomId) {
        Classroom classroom = classroomRepository.findByIdWithStudents(classroomId)
                .orElseThrow(() -> new EntityNotFoundException("Klas niet gevonden met id: " + classroomId));

        if (classroom.getTeacher() == null || !classroom.getTeacher().getId().equals(teacherId)) {
            throw new SecurityException("Je bent niet de leerkracht van deze klas.");
        }

        return classroom.getStudents().stream()
                .map(this::toStudentPreview)
                .sorted(Comparator.comparing(p -> p.getName() != null ? p.getName().toLowerCase() : ""))
                .toList();
    }

    public boolean teacherCanViewStudent(Long teacherId, Long studentId) {
        return classroomRepository.teacherHasStudent(teacherId, studentId);
    }

    private StudentPreviewDTO toStudentPreview(User student) {
        Optional<LocalDate> last = loanRepository.findByUserId(student.getId()).stream()
                .map(Loan::getStart)
                .filter(d -> d != null)
                .max(Comparator.naturalOrder());

        Map<String, Object> user = lookupService.getUser(student.getSchool(), student.getOneRosterId(), "student");

        String name = (String) user.get("fullname");

        return new StudentPreviewDTO(
                student.getId(),
                student.getUsername(),
                name,
                last.orElse(null));
    }
}
