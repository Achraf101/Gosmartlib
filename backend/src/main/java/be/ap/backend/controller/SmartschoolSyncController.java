package be.ap.backend.controller;

import be.ap.backend.service.SmartschoolSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller responsible for triggering Smartschool synchronization
 * for a given school.
 *
 * <p>
 * This endpoint delegates the full synchronization process to the
 * {@link SmartschoolSyncService} and returns a simple status message
 * containing the synced school's subdomain.
 * </p>
 */
@RestController
@RequestMapping("/smartschool")
@RequiredArgsConstructor
public class SmartschoolSyncController {

    private final SmartschoolSyncService syncService;

    /**
     * Triggers a full synchronization for the specified school.
     *
     * @param schoolId the ID of the school to synchronize
     * @return a confirmation message containing the school subdomain
     */
    @PostMapping("/sync/{schoolId}")
    public ResponseEntity<String> sync(@PathVariable Long schoolId) {
        String subdomain = syncService.syncSchool(schoolId);
        return ResponseEntity.ok("Sync completed for school: " + subdomain);
    }
}