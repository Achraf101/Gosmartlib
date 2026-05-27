package be.ap.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.StudentReportDTO;
import be.ap.backend.entity.UserRole;
import be.ap.backend.service.ClassroomService;
import be.ap.backend.service.StudentReportService;

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

    @GetMapping("/{id}/report")
    public ResponseEntity<StudentReportDTO> getReport(@PathVariable Long id) {
        Long teacherId = sessionContext.getUserId();
        if (teacherId == null) {
            // throw new MissingSessionException("Niet ingelogd.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Niet ingelogd.");
        }
        if (!sessionContext.hasRole(UserRole.LEERKRACHT)) {
            // throw new MissingSessionException("Toegang geweigerd.");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Alleen leerkrachten hebben toegang.");
        }
        if (!classroomService.teacherCanViewStudent(teacherId, id)) {
            // throw new UnauthorizedAccessException("Deze leerling zit niet in een van jouw
            // klassen.")
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Deze leerling zit niet in een van jouw klassen.");
        }
        return ResponseEntity.ok(studentReportService.buildReport(id));
    }

}
