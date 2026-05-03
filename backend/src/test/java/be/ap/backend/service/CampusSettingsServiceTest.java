package be.ap.backend.service;

import be.ap.backend.dto.CampusSettingsDTO;
import be.ap.backend.dto.HiddenComponentDTO;
import be.ap.backend.entity.Campus;
import be.ap.backend.entity.CampusSettings;
import be.ap.backend.entity.HiddenComponent;
import be.ap.backend.enums.ComponentScreen;
import be.ap.backend.enums.ComponentType;
import be.ap.backend.repository.CampusRepository;
import be.ap.backend.repository.CampusSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CampusSettingsServiceTest {

    @Mock private CampusSettingsRepository campusSettingsRepository;
    @Mock private CampusRepository campusRepository;

    @InjectMocks private CampusSettingsService campusSettingsService;

    private Campus campus;
    private CampusSettings settings;

    @BeforeEach
    void setUp() {
        campus = new Campus();
        campus.setId(1L);

        settings = new CampusSettings();
        settings.setCampus(campus);
        settings.setCampusId(1L);
        settings.setHiddenComponents(new HashSet<>());
    }

    @Test
    void getSettings_existingCampus_returnsDTO() {
        HiddenComponent component = new HiddenComponent(ComponentScreen.DASHBOARD, ComponentType.TOP_BOOKS);
        settings.setHiddenComponents(Set.of(component));

        when(campusSettingsRepository.findByCampusId(1L)).thenReturn(Optional.of(settings));

        CampusSettingsDTO result = campusSettingsService.getSettings(1L);

        assertThat(result.campusId()).isEqualTo(1L);
        assertThat(result.hiddenComponents()).hasSize(1);
        assertThat(result.hiddenComponents().iterator().next().screen()).isEqualTo("DASHBOARD");
        assertThat(result.hiddenComponents().iterator().next().type()).isEqualTo("TOP_BOOKS");
    }

    @Test
    void getSettings_noSettingsExist_returnsDefaultEmptyDTO() {
        when(campusSettingsRepository.findByCampusId(1L)).thenReturn(Optional.empty());

        CampusSettingsDTO result = campusSettingsService.getSettings(1L);

        assertThat(result.campusId()).isEqualTo(1L);
        assertThat(result.hiddenComponents()).isEmpty();
    }

    @Test
    void updateSettings_createsNewSettings() {
        HiddenComponentDTO dto = new HiddenComponentDTO("DASHBOARD", "TOP_BOOKS");
        CampusSettingsDTO input = new CampusSettingsDTO(1L, Set.of(dto));

        when(campusRepository.findById(1L)).thenReturn(Optional.of(campus));
        when(campusSettingsRepository.findByCampusId(1L)).thenReturn(Optional.empty());
        when(campusSettingsRepository.save(any())).thenReturn(settings);

        campusSettingsService.updateSettings(1L, input);

        verify(campusSettingsRepository).save(any());
    }

    @Test
    void updateSettings_updatesExistingSettings() {
        HiddenComponentDTO dto = new HiddenComponentDTO("HOME", "MONTHLY_BOOK");
        CampusSettingsDTO input = new CampusSettingsDTO(1L, Set.of(dto));

        when(campusRepository.findById(1L)).thenReturn(Optional.of(campus));
        when(campusSettingsRepository.findByCampusId(1L)).thenReturn(Optional.of(settings));
        when(campusSettingsRepository.save(any())).thenReturn(settings);

        campusSettingsService.updateSettings(1L, input);

        verify(campusSettingsRepository).save(any());
    }

    @Test
    void updateSettings_emptyHiddenComponents_savesEmptySet() {
        CampusSettingsDTO input = new CampusSettingsDTO(1L, new HashSet<>());

        when(campusRepository.findById(1L)).thenReturn(Optional.of(campus));
        when(campusSettingsRepository.findByCampusId(1L)).thenReturn(Optional.of(settings));
        when(campusSettingsRepository.save(any())).thenReturn(settings);

        campusSettingsService.updateSettings(1L, input);

        verify(campusSettingsRepository).save(argThat(s -> s.getHiddenComponents().isEmpty()));
    }
}