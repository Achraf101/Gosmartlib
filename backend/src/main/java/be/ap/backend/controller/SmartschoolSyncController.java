package be.ap.backend.controller;

import be.ap.backend.service.SmartschoolSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/smartschool")
@RequiredArgsConstructor
public class SmartschoolSyncController {

    private final SmartschoolSyncService syncService;

    @PostMapping("/sync/{schoolId}")
    public ResponseEntity<String> sync(@PathVariable Long schoolId) {
        String subdomain = syncService.syncSchool(schoolId);
        return ResponseEntity.ok("Sync completed for school: " + subdomain);
    }
}