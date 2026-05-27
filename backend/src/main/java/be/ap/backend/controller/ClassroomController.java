package be.ap.backend.controller;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.ClassroomDTO;
import be.ap.backend.dto.StudentPreviewDTO;
import be.ap.backend.entity.UserRole;
import be.ap.backend.service.ClassroomService;

@RestController
@RequestMapping("classrooms")
public class ClassroomController {

    private final ClassroomService classroomService;
    private final SessionContext sessionContext;

    public ClassroomController(ClassroomService classroomService, SessionContext sessionContext) {
        this.classroomService = classroomService;
        this.sessionContext = sessionContext;

    }

    @GetMapping
    public ResponseEntity<List<ClassroomDTO>> getMyClassrooms() {
        Long teacherId = requireTeacher();
        return ResponseEntity.ok(classroomService.getClassroomsForTeacher(teacherId));
    }

    @GetMapping("/{id}/students")
    public ResponseEntity<List<StudentPreviewDTO>> getStudents(@PathVariable Long id) {
        Long teacherId = requireTeacher();
        try {
            return ResponseEntity.ok(classroomService.getStudentsForClassroom(teacherId, id));
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    private Long requireTeacher() {
        if (!sessionContext.hasRole(UserRole.LEERKRACHT)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Alleen leerkrachten hebben toegang.");
        }
        Long userId = sessionContext.getUserId();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Geen gebruiker in sessie.");
        }
        return userId;
    }
}
