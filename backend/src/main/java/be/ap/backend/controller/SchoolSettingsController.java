package be.ap.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.SchoolSettingsDTO;
import be.ap.backend.exception.MissingSessionException;
import be.ap.backend.service.SchoolSettingsService;
import lombok.AllArgsConstructor;

/**
 * REST controller for managing school-specific configuration settings.
 *
 * <p>
 * All operations are scoped to the currently authenticated school, which is
 * resolved from the {@link SessionContext}.
 * </p>
 *
 * <p>
 * A request without a valid school session will result in a
 * {@link MissingSessionException}.
 * </p>
 */
@RestController
@AllArgsConstructor
@RequestMapping("school-settings")
public class SchoolSettingsController {

    private final SchoolSettingsService schoolSettingsService;
    private final SessionContext sessionContext;

    /**
     * Retrieves the settings for the current school.
     *
     * @return the current school settings
     */
    @GetMapping
    public ResponseEntity<SchoolSettingsDTO> getSettings() {
        Object raw = sessionContext.getSchoolId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long schoolId = Long.valueOf(raw.toString());
        return ResponseEntity.ok(schoolSettingsService.getSettings(schoolId));
    }

    /**
     * Updates the settings for the current school.
     *
     * @param dto new settings values
     * @return the updated settings
     */
    @PutMapping
    public ResponseEntity<SchoolSettingsDTO> updateSettings(@RequestBody SchoolSettingsDTO dto) {
        Object raw = sessionContext.getSchoolId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long schoolId = Long.valueOf(raw.toString());
        return ResponseEntity.ok(schoolSettingsService.updateSettings(schoolId, dto));
    }
}
