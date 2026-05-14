package be.ap.backend.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LocationSettingsDTO(
        @JsonProperty("locationId") Long locationId,
        @JsonProperty("hiddenComponents") Set<HiddenComponentDTO> hiddenComponents) {
    public boolean isVisible(String screen, String type) {
        return hiddenComponents.stream()
                .noneMatch(c -> c.screen().equals(screen) && c.type().equals(type));
    }
}