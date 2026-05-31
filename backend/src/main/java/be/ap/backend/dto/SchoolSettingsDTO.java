package be.ap.backend.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing a school's UI configuration, defining which components are
 * hidden per screen.
 */
public record SchoolSettingsDTO(
        @JsonProperty("schoolId") Long schoolId,
        @JsonProperty("hiddenComponents") Set<HiddenComponentDTO> hiddenComponents) {

    /**
     * Returns {@code true} if the given component is not hidden on the specified
     * screen.
     */
    public boolean isVisible(String screen, String type) {
        return hiddenComponents.stream()
                .noneMatch(c -> c.screen().equals(screen) && c.type().equals(type));
    }
}