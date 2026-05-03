package be.ap.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.dto.CampusSettingsDTO;
import be.ap.backend.service.CampusSettingsService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("campus-settings")
public class CampusSettingsController {

    private final CampusSettingsService campusSettingsService;

    public CampusSettingsController(CampusSettingsService campusSettingsService) {
        this.campusSettingsService = campusSettingsService;
    }

    @GetMapping
    public ResponseEntity<CampusSettingsDTO> getSettings(HttpSession session) {
        Long campusId = Long.valueOf(session.getAttribute("campus").toString());
        return ResponseEntity.ok(campusSettingsService.getSettings(campusId));
    }

    @PutMapping
    public ResponseEntity<CampusSettingsDTO> updateSettings(@RequestBody CampusSettingsDTO dto, HttpSession session) {
        Long campusId = Long.valueOf(session.getAttribute("campus").toString());
        return ResponseEntity.ok(campusSettingsService.updateSettings(campusId, dto));
    }
}
