package be.ap.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * DTO representing a physical location belonging to a school.
 */
@Data
public class LocationDTO {
    private Long id;
    private String name;
    private String adres;
    @JsonProperty("schoolId")
    private Long schoolId;
}
