package be.ap.backend.service;

import java.util.HashSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import be.ap.backend.dto.SchoolSettingsDTO;
import be.ap.backend.dto.HiddenComponentDTO;
import be.ap.backend.entity.SchoolSettings;
import be.ap.backend.entity.School;
import be.ap.backend.entity.HiddenComponent;
import be.ap.backend.enums.ComponentScreen;
import be.ap.backend.enums.ComponentType;
import be.ap.backend.repository.SchoolSettingsRepository;
import jakarta.persistence.EntityNotFoundException;
import be.ap.backend.repository.SchoolRepository;

/**
 * Service for managing per-school UI settings, such as hidden components.
 */
@Service
public class SchoolSettingsService {

    private final SchoolSettingsRepository schoolSettingsRepository;
    private final SchoolRepository schoolRepository;

    public SchoolSettingsService(SchoolSettingsRepository schoolSettingsRepository,
            SchoolRepository schoolRepository) {
        this.schoolSettingsRepository = schoolSettingsRepository;
        this.schoolRepository = schoolRepository;
    }

    /**
     * Returns the current settings for the given school, or a default empty
     * configuration if none exist.
     */
    public SchoolSettingsDTO getSettings(Long schoolId) {
        return schoolSettingsRepository.findBySchoolId(schoolId)
                .map(s -> new SchoolSettingsDTO(
                        s.getSchoolId(),
                        s.getHiddenComponents().stream()
                                .map(c -> new HiddenComponentDTO(c.getScreen().name(), c.getType().name()))
                                .collect(Collectors.toSet())))
                .orElse(new SchoolSettingsDTO(schoolId, new HashSet<>()));
    }

    /**
     * @throws EntityNotFoundException if no school exists with the given ID
     */
    public SchoolSettingsDTO updateSettings(Long schoolId, SchoolSettingsDTO dto) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new EntityNotFoundException("School niet gevonden met id: " + schoolId));
        SchoolSettings settings = schoolSettingsRepository.findBySchoolId(schoolId)
                .orElse(new SchoolSettings());
        settings.setSchool(school);
        settings.setHiddenComponents(
                dto.hiddenComponents().stream()
                        .map(c -> new HiddenComponent(
                                ComponentScreen.valueOf(c.screen()),
                                ComponentType.valueOf(c.type())))
                        .collect(Collectors.toSet()));
        schoolSettingsRepository.save(settings);
        return getSettings(schoolId);
    }
}
