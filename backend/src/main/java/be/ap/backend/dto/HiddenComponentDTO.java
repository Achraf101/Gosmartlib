package be.ap.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing a UI component that should be hidden on a given screen.
 */
public record HiddenComponentDTO(
        @JsonProperty("screen") String screen,
        @JsonProperty("type") String type) {
}