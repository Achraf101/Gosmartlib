package be.ap.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import be.ap.backend.dto.SchoolDTO;
import be.ap.backend.service.SchoolService;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing schools.
 *
 * <p>
 * Provides endpoints for creating, retrieving, and updating school entities.
 * All operations delegate business logic to {@link SchoolService}.
 * </p>
 *
 * <p>
 * Note: This controller currently exposes DTO-based access without explicit
 * role-based protection. Authorization is expected to be enforced at service
 * or security configuration level.
 * </p>
 */
@RestController
@RequestMapping("school")
@RequiredArgsConstructor
public class SchoolController {
    private final SchoolService schoolService;

    /**
     * Creates a new school.
     *
     * @param dto the school data to create
     * @return the created school
     */
    @PostMapping
    public ResponseEntity<SchoolDTO> addSchool(@RequestBody SchoolDTO dto) {
        return ResponseEntity.ok(schoolService.addSchool(dto));
    }

    /**
     * Retrieves all schools in the system.
     *
     * @return list of all schools
     */
    @GetMapping
    public ResponseEntity<List<SchoolDTO>> getAll() {
        return ResponseEntity.ok(schoolService.getAll());
    }

    /**
     * Retrieves a school by its ID.
     *
     * @param id the school ID
     * @return the school if found, otherwise 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<SchoolDTO> findById(@PathVariable Long id) {
        return schoolService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates an existing school.
     *
     * @param id  the school ID
     * @param dto updated school data
     * @return the updated school
     */
    @PutMapping("/{id}")
    public ResponseEntity<SchoolDTO> updateSchool(@PathVariable Long id, @RequestBody SchoolDTO dto) {
        return ResponseEntity.ok(schoolService.updateSchool(id, dto));
    }
}
