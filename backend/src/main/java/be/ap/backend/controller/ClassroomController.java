package be.ap.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import be.ap.backend.dto.ClassroomDTO;
import be.ap.backend.dto.StudentPreviewDTO;
import be.ap.backend.entity.UserRole;
import be.ap.backend.service.ClassroomService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("classrooms")
public class ClassroomController {

    private final ClassroomService classroomService;

    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @GetMapping
    public ResponseEntity<List<ClassroomDTO>> getMyClassrooms(HttpSession session) {
        Long teacherId = requireTeacher(session);
        return ResponseEntity.ok(classroomService.getClassroomsForTeacher(teacherId));
    }

    @GetMapping("/{id}/students")
    public ResponseEntity<List<StudentPreviewDTO>> getStudents(@PathVariable Long id, HttpSession session) {
        Long teacherId = requireTeacher(session);
        try {
            return ResponseEntity.ok(classroomService.getStudentsForClassroom(teacherId, id));
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    private Long requireTeacher(HttpSession session) {
        Object roleRaw = session.getAttribute("role");
        Object userRaw = session.getAttribute("userId");
        if (roleRaw == null || userRaw == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Niet ingelogd.");
        }
        if (!UserRole.LEERKRACHT.name().equals(roleRaw.toString())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Alleen leerkrachten hebben toegang.");
        }
        return Long.valueOf(userRaw.toString());
    }
}
