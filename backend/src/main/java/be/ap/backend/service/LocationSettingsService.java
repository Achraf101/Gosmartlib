package be.ap.backend.service;

import java.util.HashSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import be.ap.backend.dto.LocationSettingsDTO;
import be.ap.backend.dto.HiddenComponentDTO;
import be.ap.backend.entity.Location;
import be.ap.backend.entity.LocationSettings;
import be.ap.backend.entity.HiddenComponent;
import be.ap.backend.enums.ComponentScreen;
import be.ap.backend.enums.ComponentType;
import be.ap.backend.repository.LocationRepository;
import be.ap.backend.repository.LocationSettingsRepository;

@Service
public class LocationSettingsService {

    private final LocationSettingsRepository locationSettingsRepository;
    private final LocationRepository locationRepository;

    public LocationSettingsService(LocationSettingsRepository locationSettingsRepository,
            LocationRepository locationRepository) {
        this.locationSettingsRepository = locationSettingsRepository;
        this.locationRepository = locationRepository;
    }

    public LocationSettingsDTO getSettings(Long locationId) {
        return locationSettingsRepository.findByLocationId(locationId)
                .map(s -> new LocationSettingsDTO(
                        s.getLocationId(),
                        s.getHiddenComponents().stream()
                                .map(c -> new HiddenComponentDTO(c.getScreen().name(), c.getType().name()))
                                .collect(Collectors.toSet())))
                .orElse(new LocationSettingsDTO(locationId, new HashSet<>()));
    }

    public LocationSettingsDTO updateSettings(Long locationId, LocationSettingsDTO dto) {
        Location location = locationRepository.findById(locationId).orElseThrow();
        LocationSettings settings = locationSettingsRepository.findByLocationId(locationId)
                .orElse(new LocationSettings());
        settings.setLocation(location);
        settings.setHiddenComponents(
                dto.hiddenComponents().stream()
                        .map(c -> new HiddenComponent(
                                ComponentScreen.valueOf(c.screen()),
                                ComponentType.valueOf(c.type())))
                        .collect(Collectors.toSet()));
        locationSettingsRepository.save(settings);
        return getSettings(locationId);
    }
}
