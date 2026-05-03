package be.ap.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HiddenComponentDTO(
    @JsonProperty("screen") String screen,
    @JsonProperty("type") String type
) {}