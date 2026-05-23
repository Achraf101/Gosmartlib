package be.ap.backend.service;

import be.ap.backend.dto.SchoolSettingsDTO;
import be.ap.backend.dto.HiddenComponentDTO;
import be.ap.backend.entity.SchoolSettings;
import be.ap.backend.entity.School;
import be.ap.backend.entity.HiddenComponent;
import be.ap.backend.enums.ComponentScreen;
import be.ap.backend.enums.ComponentType;
import be.ap.backend.repository.SchoolSettingsRepository;
import be.ap.backend.repository.SchoolRepository;

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
public class SchoolSettingsServiceTest {

    @Mock
    private SchoolSettingsRepository schoolSettingsRepository;
    @Mock
    private SchoolRepository schoolRepository;

    @InjectMocks
    private SchoolSettingsService schoolSettingsService;

    private School school;
    private SchoolSettings settings;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1L);

        settings = new SchoolSettings();
        settings.setSchool(school);
        settings.setSchoolId(1L);
        settings.setHiddenComponents(new HashSet<>());
    }

    @Test
    void getSettings_existingSchool_returnsDTO() {
        HiddenComponent component = new HiddenComponent(ComponentScreen.DASHBOARD, ComponentType.TOP_BOOKS);
        settings.setHiddenComponents(Set.of(component));

        when(schoolSettingsRepository.findBySchoolId(1L)).thenReturn(Optional.of(settings));

        SchoolSettingsDTO result = schoolSettingsService.getSettings(1L);

        assertThat(result.schoolId()).isEqualTo(1L);
        assertThat(result.hiddenComponents()).hasSize(1);
        assertThat(result.hiddenComponents().iterator().next().screen()).isEqualTo("DASHBOARD");
        assertThat(result.hiddenComponents().iterator().next().type()).isEqualTo("TOP_BOOKS");
    }

    @Test
    void getSettings_noSettingsExist_returnsDefaultEmptyDTO() {
        when(schoolSettingsRepository.findBySchoolId(1L)).thenReturn(Optional.empty());

        SchoolSettingsDTO result = schoolSettingsService.getSettings(1L);

        assertThat(result.schoolId()).isEqualTo(1L);
        assertThat(result.hiddenComponents()).isEmpty();
    }

    @Test
    void updateSettings_createsNewSettings_returnsDTO() {
        HiddenComponentDTO dto = new HiddenComponentDTO("DASHBOARD", "TOP_BOOKS");
        SchoolSettingsDTO input = new SchoolSettingsDTO(1L, Set.of(dto));

        HiddenComponent savedComponent = new HiddenComponent(ComponentScreen.DASHBOARD, ComponentType.TOP_BOOKS);
        SchoolSettings savedSettings = new SchoolSettings();
        savedSettings.setSchool(school);
        savedSettings.setSchoolId(1L);
        savedSettings.setHiddenComponents(Set.of(savedComponent));

        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        // First call: during save path (orElse branch — returns empty to trigger new
        // SchoolSettings)
        // Second call: during the trailing getSettings(schoolId)
        when(schoolSettingsRepository.findBySchoolId(1L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(savedSettings));
        when(schoolSettingsRepository.save(any())).thenReturn(savedSettings);

        SchoolSettingsDTO result = schoolSettingsService.updateSettings(1L, input);

        verify(schoolSettingsRepository).save(any());
        assertThat(result.schoolId()).isEqualTo(1L);
        assertThat(result.hiddenComponents()).hasSize(1);
        assertThat(result.hiddenComponents().iterator().next().screen()).isEqualTo("DASHBOARD");
        assertThat(result.hiddenComponents().iterator().next().type()).isEqualTo("TOP_BOOKS");
    }

    @Test
    void updateSettings_updatesExistingSettings_returnsDTO() {
        HiddenComponentDTO dto = new HiddenComponentDTO("HOME", "MONTHLY_BOOK");
        SchoolSettingsDTO input = new SchoolSettingsDTO(1L, Set.of(dto));

        HiddenComponent savedComponent = new HiddenComponent(ComponentScreen.HOME, ComponentType.MONTHLY_BOOK);
        SchoolSettings savedSettings = new SchoolSettings();
        savedSettings.setSchool(school);
        savedSettings.setSchoolId(1L);
        savedSettings.setHiddenComponents(Set.of(savedComponent));

        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(schoolSettingsRepository.findBySchoolId(1L))
                .thenReturn(Optional.of(settings))
                .thenReturn(Optional.of(savedSettings));
        when(schoolSettingsRepository.save(any())).thenReturn(savedSettings);

        SchoolSettingsDTO result = schoolSettingsService.updateSettings(1L, input);

        verify(schoolSettingsRepository).save(any());
        assertThat(result.schoolId()).isEqualTo(1L);
        assertThat(result.hiddenComponents()).hasSize(1);
        assertThat(result.hiddenComponents().iterator().next().screen()).isEqualTo("HOME");
        assertThat(result.hiddenComponents().iterator().next().type()).isEqualTo("MONTHLY_BOOK");
    }

    @Test
    void updateSettings_emptyHiddenComponents_savesEmptySetAndReturnsDTO() {
        SchoolSettingsDTO input = new SchoolSettingsDTO(1L, new HashSet<>());

        SchoolSettings savedSettings = new SchoolSettings();
        savedSettings.setSchool(school);
        savedSettings.setSchoolId(1L);
        savedSettings.setHiddenComponents(new HashSet<>());

        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(schoolSettingsRepository.findBySchoolId(1L))
                .thenReturn(Optional.of(settings))
                .thenReturn(Optional.of(savedSettings));
        when(schoolSettingsRepository.save(any())).thenReturn(savedSettings);

        SchoolSettingsDTO result = schoolSettingsService.updateSettings(1L, input);

        verify(schoolSettingsRepository).save(argThat(s -> s.getHiddenComponents().isEmpty()));
        assertThat(result.schoolId()).isEqualTo(1L);
        assertThat(result.hiddenComponents()).isEmpty();
    }
}