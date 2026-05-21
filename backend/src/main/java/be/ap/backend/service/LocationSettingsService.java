package be.ap.backend.service;

import java.util.HashSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import be.ap.backend.dto.LocationSettingsDTO;
import be.ap.backend.dto.HiddenComponentDTO;
import be.ap.backend.entity.LocationSettings;
import be.ap.backend.entity.School;
import be.ap.backend.entity.HiddenComponent;
import be.ap.backend.enums.ComponentScreen;
import be.ap.backend.enums.ComponentType;
import be.ap.backend.repository.LocationSettingsRepository;
import be.ap.backend.repository.SchoolRepository;

@Service
public class LocationSettingsService {

    private final LocationSettingsRepository locationSettingsRepository;
    private final SchoolRepository schoolRepository;

    public LocationSettingsService(LocationSettingsRepository locationSettingsRepository,
            SchoolRepository schoolRepository) {
        this.locationSettingsRepository = locationSettingsRepository;
        this.schoolRepository = schoolRepository;
    }

    public LocationSettingsDTO getSettings(Long schoolId) {
        return locationSettingsRepository.findBySchoolId(schoolId)
                .map(s -> new LocationSettingsDTO(
                        s.getSchoolId(),
                        s.getHiddenComponents().stream()
                                .map(c -> new HiddenComponentDTO(c.getScreen().name(), c.getType().name()))
                                .collect(Collectors.toSet())))
                .orElse(new LocationSettingsDTO(schoolId, new HashSet<>()));
    }

    public LocationSettingsDTO updateSettings(Long schoolId, LocationSettingsDTO dto) {
        School school = schoolRepository.findById(schoolId).orElseThrow();
        LocationSettings settings = locationSettingsRepository.findBySchoolId(schoolId)
                .orElse(new LocationSettings());
        settings.setSchool(school);
        settings.setHiddenComponents(
                dto.hiddenComponents().stream()
                        .map(c -> new HiddenComponent(
                                ComponentScreen.valueOf(c.screen()),
                                ComponentType.valueOf(c.type())))
                        .collect(Collectors.toSet()));
        locationSettingsRepository.save(settings);
        return getSettings(schoolId);
    }
}
