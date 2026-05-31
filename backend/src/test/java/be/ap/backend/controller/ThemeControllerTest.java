package be.ap.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.ap.backend.dto.ThemeDTO;
import be.ap.backend.entity.Theme;
import be.ap.backend.service.ThemeService;

@ExtendWith(MockitoExtension.class)
public class ThemeControllerTest {

    @Mock
    private ThemeService themeService;

    @InjectMocks
    private ThemeController controller;

    // ── GET /theme ────────────────────────────────────────────────────────────

    @Test
    void givenThemesExist_whenGetAll_thenReturnAllThemes() {
        ThemeDTO theme1 = new ThemeDTO(1L, "Adventure");
        ThemeDTO theme2 = new ThemeDTO(2L, "Romance");
        when(themeService.getAll()).thenReturn(List.of(theme1, theme2));

        List<ThemeDTO> result = controller.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Adventure", result.get(0).getName());
        assertEquals("Romance", result.get(1).getName());
        verify(themeService, times(1)).getAll();
    }

    @Test
    void givenNoThemes_whenGetAll_thenReturnEmptyList() {
        when(themeService.getAll()).thenReturn(Collections.emptyList());

        List<ThemeDTO> result = controller.getAll();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(themeService, times(1)).getAll();
    }

    // ── POST /theme ───────────────────────────────────────────────────────────

    @Test
    void givenValidTheme_whenAddTheme_thenReturnDTO() {
        Theme theme = new Theme();
        theme.setName("Mystery");

        Theme savedTheme = new Theme();
        savedTheme.setName("Mystery");

        ThemeDTO expectedDTO = new ThemeDTO(3L, "Mystery");

        when(themeService.add(theme)).thenReturn(savedTheme);
        when(themeService.convertToDTO(savedTheme)).thenReturn(expectedDTO);

        ThemeDTO result = controller.addTheme(theme);

        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("Mystery", result.getName());
    }

    @Test
    void givenValidTheme_whenAddTheme_thenServiceCalledOnce() {
        Theme theme = new Theme();
        theme.setName("Thriller");

        Theme savedTheme = new Theme();
        ThemeDTO dto = new ThemeDTO(4L, "Thriller");

        when(themeService.add(theme)).thenReturn(savedTheme);
        when(themeService.convertToDTO(savedTheme)).thenReturn(dto);

        controller.addTheme(theme);

        verify(themeService, times(1)).add(theme);
        verify(themeService, times(1)).convertToDTO(savedTheme);
    }

    @Test
    void givenThemeWithNoName_whenAddTheme_thenReturnDTOWithNullName() {
        Theme theme = new Theme(); // name is null

        Theme savedTheme = new Theme();
        ThemeDTO expectedDTO = new ThemeDTO(5L, null);

        when(themeService.add(theme)).thenReturn(savedTheme);
        when(themeService.convertToDTO(savedTheme)).thenReturn(expectedDTO);

        ThemeDTO result = controller.addTheme(theme);

        assertNotNull(result);
        assertNull(result.getName());
        verify(themeService, times(1)).add(theme);
        verify(themeService, times(1)).convertToDTO(savedTheme);
    }
}