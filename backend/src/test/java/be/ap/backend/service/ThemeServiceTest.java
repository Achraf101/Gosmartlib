package be.ap.backend.service;

import be.ap.backend.dto.ThemeDTO;
import be.ap.backend.entity.Theme;
import be.ap.backend.repository.ThemeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private ThemeService themeService;

    @Test
    void getAll_returnsEmptyList_whenRepositoryIsEmpty() {
        when(themeRepository.findAll()).thenReturn(List.of());

        List<ThemeDTO> result = themeService.getAll();

        assertThat(result).isEmpty();
        verify(themeRepository, times(1)).findAll();
    }

    @Test
    void getAll_returnsMappedDTOs_whenRepositoryHasEntities() {
        Theme theme1 = buildTheme(1L, "Liefde & relaties");
        Theme theme2 = buildTheme(2L, "Mentale gezondheid");

        when(themeRepository.findAll()).thenReturn(List.of(theme1, theme2));

        List<ThemeDTO> result = themeService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("Liefde & relaties");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("Mentale gezondheid");
    }

    @Test
    void getAll_mapsAllFields() {
        Theme theme = buildTheme(3L, "Identiteit & zelfbeeld");

        when(themeRepository.findAll()).thenReturn(List.of(theme));

        ThemeDTO result = themeService.getAll().get(0);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("Identiteit & zelfbeeld");
    }

    @Test
    void getAll_preservesOrder() {
        List<Theme> themes = List.of(
                buildTheme(1L, "Rouw & verlies"),
                buildTheme(2L, "Familie"),
                buildTheme(3L, "Sociale media"),
                buildTheme(4L, "Migratie & afkomst"));

        when(themeRepository.findAll()).thenReturn(themes);

        List<ThemeDTO> result = themeService.getAll();

        assertThat(result)
                .extracting(ThemeDTO::getId)
                .containsExactly(1L, 2L, 3L, 4L);
    }

    @Test
    void getAll_delegatesToRepositoryExactlyOnce() {
        when(themeRepository.findAll()).thenReturn(List.of());

        themeService.getAll();

        verify(themeRepository, times(1)).findAll();
        verifyNoMoreInteractions(themeRepository);
    }

    private Theme buildTheme(Long id, String name) {
        Theme theme = new Theme();
        theme.setId(id);
        theme.setName(name);
        return theme;
    }
}