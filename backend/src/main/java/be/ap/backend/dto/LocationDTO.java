package be.ap.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class LocationDTO {
    private Long id;
    private String name;
    private String adres;
    @JsonProperty("schoolId")
    private Long schoolId;
}
