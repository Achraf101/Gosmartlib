package be.ap.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import be.ap.backend.dto.StudentReportDTO;
import be.ap.backend.entity.UserRole;
import be.ap.backend.service.ClassroomService;
import be.ap.backend.service.StudentReportService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("students")
public class StudentReportController {

    private final StudentReportService studentReportService;
    private final ClassroomService classroomService;

    public StudentReportController(StudentReportService studentReportService, ClassroomService classroomService) {
        this.studentReportService = studentReportService;
        this.classroomService = classroomService;
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<StudentReportDTO> getReport(@PathVariable Long id, HttpSession session) {
        Object roleRaw = session.getAttribute("role");
        Object userRaw = session.getAttribute("userId");
        if (roleRaw == null || userRaw == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Niet ingelogd.");
        }
        if (!UserRole.LEERKRACHT.name().equals(roleRaw.toString())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Alleen leerkrachten hebben toegang.");
        }
        Long teacherId = Long.valueOf(userRaw.toString());
        if (!classroomService.teacherCanViewStudent(teacherId, id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Deze leerling zit niet in een van jouw klassen.");
        }
        return ResponseEntity.ok(studentReportService.buildReport(id));
    }
}
