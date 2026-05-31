package be.ap.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.StudentReportDTO;
import be.ap.backend.entity.UserRole;
import be.ap.backend.exception.MissingSessionException;
import be.ap.backend.exception.UnauthorizedAccessException;
import be.ap.backend.service.ClassroomService;
import be.ap.backend.service.StudentReportService;

/**
 * REST controller responsible for retrieving academic reports for students.
 *
 * <p>
 * Access is restricted to teachers who are authenticated and authorized
 * to view the requested student (i.e. the student belongs to one of their
 * classrooms).
 * </p>
 *
 * <p>
 * This controller enforces a combination of:
 * <ul>
 * <li>Session-based authentication via {@link SessionContext}</li>
 * <li>Role-based access control (LEERKRACHT)</li>
 * <li>Domain-level authorization via {@link ClassroomService}</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("students")
public class StudentReportController {

    private final StudentReportService studentReportService;
    private final ClassroomService classroomService;
    private final SessionContext sessionContext;

    public StudentReportController(StudentReportService studentReportService, ClassroomService classroomService,
            SessionContext sessionContext) {
        this.studentReportService = studentReportService;
        this.classroomService = classroomService;
        this.sessionContext = sessionContext;
    }

    /**
     * Retrieves a detailed report for a specific student.
     *
     * <p>
     * Access rules:
     * </p>
     * <ul>
     * <li>User must be logged in</li>
     * <li>User must have role LEERKRACHT</li>
     * <li>User must be authorized to access the student via classroom
     * membership</li>
     * </ul>
     *
     * @param id the student ID
     * @return the student's report
     */
    @GetMapping("/{id}/report")
    public ResponseEntity<StudentReportDTO> getReport(@PathVariable Long id) {
        Long teacherId = sessionContext.getUserId();
        if (teacherId == null) {
            throw new MissingSessionException("Niet ingelogd.");
        }
        if (!sessionContext.hasRole(UserRole.LEERKRACHT)) {
            throw new MissingSessionException("Toegang geweigerd.");
        }
        if (!classroomService.teacherCanViewStudent(teacherId, id)) {
            throw new UnauthorizedAccessException("Deze leerling zit niet in een van jouw klassen.");
        }
        return ResponseEntity.ok(studentReportService.buildReport(id));
    }

}
