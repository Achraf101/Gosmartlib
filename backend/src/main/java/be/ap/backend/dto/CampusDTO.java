package be.ap.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CampusDTO {
    private Long id;
    private String name;
    private String adres;
    @JsonProperty("borrowLimit")
    private int borrowLimit;
    @JsonProperty("borrowPeriod")
    private int borrowPeriod;
    @JsonProperty("extendLimit")
    private int extendLimit;
    @JsonProperty("extendPeriod")
    private int extendPeriod;
    @JsonProperty("schoolId")
    private Long schoolId;
}
