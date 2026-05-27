package be.ap.backend.controller;

import be.ap.backend.entity.School;
import be.ap.backend.repository.SchoolRepository;
import be.ap.backend.service.SmartschoolSyncService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/smartschool")
@RequiredArgsConstructor
public class SmartschoolSyncController {

    private final SmartschoolSyncService syncService;
    private final SchoolRepository schoolRepository;

    @PostMapping("/sync/{schoolId}")
    public ResponseEntity<String> sync(@PathVariable Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new EntityNotFoundException("School niet gevonden: " + schoolId));

        if (school.getOneRosterClientId() == null || school.getOneRosterClientSecret() == null) {
            return ResponseEntity.badRequest().body("School has no OneRoster credentials configured");
        }

        syncService.syncSchool(school);
        return ResponseEntity.ok("Sync completed for school: " + school.getSsSubdomain());
    }
}