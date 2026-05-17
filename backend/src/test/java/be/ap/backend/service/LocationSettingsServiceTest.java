package be.ap.backend.service;

import be.ap.backend.dto.LocationSettingsDTO;
import be.ap.backend.dto.HiddenComponentDTO;
import be.ap.backend.entity.Location;
import be.ap.backend.entity.LocationSettings;
import be.ap.backend.entity.HiddenComponent;
import be.ap.backend.enums.ComponentScreen;
import be.ap.backend.enums.ComponentType;
import be.ap.backend.repository.LocationRepository;
import be.ap.backend.repository.LocationSettingsRepository;
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
public class LocationSettingsServiceTest {

    @Mock
    private LocationSettingsRepository locationSettingsRepository;
    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationSettingsService locationSettingsService;

    private Location location;
    private LocationSettings settings;

    @BeforeEach
    void setUp() {
        location = new Location();
        location.setId(1L);

        settings = new LocationSettings();
        settings.setLocation(location);
        settings.setLocationId(1L);
        settings.setHiddenComponents(new HashSet<>());
    }

    @Test
    void getSettings_existingLocation_returnsDTO() {
        HiddenComponent component = new HiddenComponent(ComponentScreen.DASHBOARD, ComponentType.TOP_BOOKS);
        settings.setHiddenComponents(Set.of(component));

        when(locationSettingsRepository.findByLocationId(1L)).thenReturn(Optional.of(settings));

        LocationSettingsDTO result = locationSettingsService.getSettings(1L);

        assertThat(result.locationId()).isEqualTo(1L);
        assertThat(result.hiddenComponents()).hasSize(1);
        assertThat(result.hiddenComponents().iterator().next().screen()).isEqualTo("DASHBOARD");
        assertThat(result.hiddenComponents().iterator().next().type()).isEqualTo("TOP_BOOKS");
    }

    @Test
    void getSettings_noSettingsExist_returnsDefaultEmptyDTO() {
        when(locationSettingsRepository.findByLocationId(1L)).thenReturn(Optional.empty());

        LocationSettingsDTO result = locationSettingsService.getSettings(1L);

        assertThat(result.locationId()).isEqualTo(1L);
        assertThat(result.hiddenComponents()).isEmpty();
    }

    @Test
    void updateSettings_createsNewSettings() {
        HiddenComponentDTO dto = new HiddenComponentDTO("DASHBOARD", "TOP_BOOKS");
        LocationSettingsDTO input = new LocationSettingsDTO(1L, Set.of(dto));

        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(locationSettingsRepository.findByLocationId(1L)).thenReturn(Optional.empty());
        when(locationSettingsRepository.save(any())).thenReturn(settings);

        locationSettingsService.updateSettings(1L, input);

        verify(locationSettingsRepository).save(any());
    }

    @Test
    void updateSettings_updatesExistingSettings() {
        HiddenComponentDTO dto = new HiddenComponentDTO("HOME", "MONTHLY_BOOK");
        LocationSettingsDTO input = new LocationSettingsDTO(1L, Set.of(dto));

        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(locationSettingsRepository.findByLocationId(1L)).thenReturn(Optional.of(settings));
        when(locationSettingsRepository.save(any())).thenReturn(settings);

        locationSettingsService.updateSettings(1L, input);

        verify(locationSettingsRepository).save(any());
    }

    @Test
    void updateSettings_emptyHiddenComponents_savesEmptySet() {
        LocationSettingsDTO input = new LocationSettingsDTO(1L, new HashSet<>());

        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(locationSettingsRepository.findByLocationId(1L)).thenReturn(Optional.of(settings));
        when(locationSettingsRepository.save(any())).thenReturn(settings);

        locationSettingsService.updateSettings(1L, input);

        verify(locationSettingsRepository).save(argThat(s -> s.getHiddenComponents().isEmpty()));
    }
}