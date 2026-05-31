package be.ap.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.dto.ThemeDTO;
import be.ap.backend.entity.Theme;
import be.ap.backend.service.ThemeService;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing themes.
 *
 * <p>
 * Provides endpoints for retrieving all themes and creating new themes.
 * This controller delegates business logic and persistence to
 * {@link ThemeService}.
 * </p>
 */
@RestController
@RequestMapping("theme")
@RequiredArgsConstructor
public class ThemeController {
    private final ThemeService themeService;

    /**
     * Retrieves all available themes.
     *
     * @return list of theme DTOs
     */
    @GetMapping()
    public List<ThemeDTO> getAll() {
        return themeService.getAll();
    }

    /**
     * Creates a new theme.
     *
     * @param theme the theme entity to persist
     * @return the created theme as DTO
     */
    @PostMapping
    public ThemeDTO addTheme(@RequestBody Theme theme) {
        return themeService.convertToDTO(themeService.add(theme));
    }
}
