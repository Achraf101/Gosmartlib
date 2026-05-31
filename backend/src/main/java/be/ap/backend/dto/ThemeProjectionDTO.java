package be.ap.backend.dto;

/**
 * Projection mapping a book to one of its themes.
 */
public interface ThemeProjectionDTO {
    Long getBookId();

    Long getThemeId();

    String getThemeName();
}
