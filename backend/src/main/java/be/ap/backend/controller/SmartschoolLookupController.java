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

/**
 * REST controller for external Smartschool data lookups.
 *
 * <p>
 * Provides endpoints to retrieve users, classes, and teachers from an
 * external Smartschool integration per school context.
 * </p>
 *
 * <p>
 * Data retrieval is delegated to {@link SmartschoolLookupService}.
 * </p>
 */
@RestController
@RequestMapping("/smartschool/lookup")
@RequiredArgsConstructor
public class SmartschoolLookupController {

    private final SmartschoolLookupService lookupService;

    /**
     * Retrieves a user from Smartschool by school and Smartschool ID.
     *
     * @param schoolId target school
     * @param ssId     Smartschool identifier
     * @param role     user role hint (student or teacher)
     * @return user data if found, otherwise 404
     */
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

    /**
     * Retrieves a Smartschool classroom by external ID.
     *
     * @param schoolId school context
     * @param ssId     Smartschool class identifier
     * @return classroom data or 404 if not found
     */
    @GetMapping("/{schoolId}/classes/{ssId}")
    public ResponseEntity<Map<String, Object>> getClass(
            @PathVariable Long schoolId,
            @PathVariable String ssId) {
        Map<String, Object> classroom = lookupService.getClassroom(schoolId, ssId);
        if (classroom == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(classroom);
    }

    /**
     * Retrieves all teachers for a given school from Smartschool.
     *
     * @param schoolId school identifier
     * @return list of teachers
     */
    @GetMapping("/{schoolId}/teachers")
    public ResponseEntity<List<TeacherDTO>> getAllTeachersForSchool(@PathVariable Long schoolId) {
        List<TeacherDTO> teachers = lookupService.getAllTeachersForSchool(schoolId);
        return ResponseEntity.ok(teachers);
    }
}