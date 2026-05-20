package be.ap.backend.controller;

import be.ap.backend.entity.School;
import be.ap.backend.repository.SchoolRepository;
import be.ap.backend.service.SmartschoolLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/smartschool/lookup")
@RequiredArgsConstructor
public class SmartschoolLookupController {

    private final SmartschoolLookupService lookupService;
    private final SchoolRepository schoolRepository;

    @GetMapping("/{schoolId}/users/{ssId}")
    public ResponseEntity<Map<String, Object>> getUser(
            @PathVariable Long schoolId,
            @PathVariable String ssId,
            @RequestParam String role) {
        School school = getSchool(schoolId);
        Map<String, Object> user = lookupService.getUser(school, ssId, role);
        if (user == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{schoolId}/classes/{ssId}")
    public ResponseEntity<Map<String, Object>> getClass(
            @PathVariable Long schoolId,
            @PathVariable String ssId) {
        School school = getSchool(schoolId);
        Map<String, Object> classroom = lookupService.getClass(school, ssId);
        if (classroom == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(classroom);
    }

    @GetMapping("/{schoolId}/users/{ssId}/classes")
    public ResponseEntity<List<Map<String, Object>>> getClassesForUser(
            @PathVariable Long schoolId,
            @PathVariable String ssId) {
        School school = getSchool(schoolId);
        List<Map<String, Object>> classes = lookupService.getClassesForUser(school, ssId);
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/{schoolId}/users/{ssId}/enrollments")
    public ResponseEntity<List<Map<String, Object>>> getEnrollmentsForUser(
            @PathVariable Long schoolId,
            @PathVariable String ssId) {
        School school = getSchool(schoolId);
        List<Map<String, Object>> enrollments = lookupService.getEnrollmentsForUser(school, ssId);
        return ResponseEntity.ok(enrollments);
    }

    private School getSchool(Long schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found: " + schoolId));
    }
}