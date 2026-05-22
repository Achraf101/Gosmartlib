package be.ap.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.dto.LocationSettingsDTO;
import be.ap.backend.service.LocationSettingsService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("location-settings")
public class LocationSettingsController {

    private final LocationSettingsService locationSettingsService;

    public LocationSettingsController(LocationSettingsService locationSettingsService) {
        this.locationSettingsService = locationSettingsService;
    }

    @GetMapping
    public ResponseEntity<LocationSettingsDTO> getSettings(HttpSession session) {
        Long schoolId = Long.valueOf(session.getAttribute("school").toString());
        return ResponseEntity.ok(locationSettingsService.getSettings(schoolId));
    }

    @PutMapping
    public ResponseEntity<LocationSettingsDTO> updateSettings(@RequestBody LocationSettingsDTO dto,
            HttpSession session) {
        Long schoolId = Long.valueOf(session.getAttribute("school").toString());
        return ResponseEntity.ok(locationSettingsService.updateSettings(schoolId, dto));
    }
}
