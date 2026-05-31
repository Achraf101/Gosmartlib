package be.ap.backend.controller;

import be.ap.backend.dto.TeacherDTO;
import be.ap.backend.entity.UserRole;
import be.ap.backend.service.SmartschoolLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/smartschool/lookup")
@RequiredArgsConstructor
public class SmartschoolLookupController {

    private final SmartschoolLookupService lookupService;

    @GetMapping("/{schoolId}/users/{ssId}")
    public ResponseEntity<Map<String, Object>> getUser(
            @PathVariable Long schoolId,
            @PathVariable String ssId,
            @RequestParam String role) {
        UserRole userRole = "student".equalsIgnoreCase(role) ? UserRole.STUDENT : UserRole.LEERKRACHT;
        Map<String, Object> user = lookupService.getUser(schoolId, ssId, Set.of(userRole));
        if (user == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{schoolId}/classes/{ssId}")
    public ResponseEntity<Map<String, Object>> getClass(
            @PathVariable Long schoolId,
            @PathVariable String ssId) {
        Map<String, Object> classroom = lookupService.getClassroom(schoolId, ssId);
        if (classroom == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(classroom);
    }

    @GetMapping("/{schoolId}/teachers")
    public ResponseEntity<List<TeacherDTO>> getAllTeachersForSchool(@PathVariable Long schoolId) {
        List<TeacherDTO> teachers = lookupService.getAllTeachersForSchool(schoolId);
        return ResponseEntity.ok(teachers);
    }
}