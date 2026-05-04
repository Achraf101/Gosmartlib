package be.ap.backend.service;

import java.util.HashSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import be.ap.backend.dto.CampusSettingsDTO;
import be.ap.backend.dto.HiddenComponentDTO;
import be.ap.backend.entity.Campus;
import be.ap.backend.entity.CampusSettings;
import be.ap.backend.entity.HiddenComponent;
import be.ap.backend.enums.ComponentScreen;
import be.ap.backend.enums.ComponentType;
import be.ap.backend.repository.CampusRepository;
import be.ap.backend.repository.CampusSettingsRepository;

@Service
public class CampusSettingsService {

    private final CampusSettingsRepository campusSettingsRepository;
    private final CampusRepository campusRepository;

    public CampusSettingsService(CampusSettingsRepository campusSettingsRepository, CampusRepository campusRepository) {
        this.campusSettingsRepository = campusSettingsRepository;
        this.campusRepository = campusRepository;
    }

    public CampusSettingsDTO getSettings(Long campusId) {
        return campusSettingsRepository.findByCampusId(campusId)
            .map(s -> new CampusSettingsDTO(
                s.getCampusId(),
                s.getHiddenComponents().stream()
                    .map(c -> new HiddenComponentDTO(c.getScreen().name(), c.getType().name()))
                    .collect(Collectors.toSet())
            ))
            .orElse(new CampusSettingsDTO(campusId, new HashSet<>()));
    }

    public CampusSettingsDTO updateSettings(Long campusId, CampusSettingsDTO dto) {
        Campus campus = campusRepository.findById(campusId).orElseThrow();
        CampusSettings settings = campusSettingsRepository.findByCampusId(campusId)
            .orElse(new CampusSettings());
        settings.setCampus(campus);
        settings.setHiddenComponents(
            dto.hiddenComponents().stream()
                .map(c -> new HiddenComponent(
                    ComponentScreen.valueOf(c.screen()),
                    ComponentType.valueOf(c.type())
                ))
                .collect(Collectors.toSet())
        );
        campusSettingsRepository.save(settings);
        return getSettings(campusId);
    }
}
