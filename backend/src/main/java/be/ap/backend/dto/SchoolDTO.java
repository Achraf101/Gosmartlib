package be.ap.backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * DTO representing a school, including its loan policy settings,
 * OneRoster integration credentials, and associated locations.
 */
@Data
public class SchoolDTO {
    private Long id;
    private String name;
    private String adres;
    private String contact;
    private String description;
    @JsonProperty("borrowLimit")
    private int borrowLimit;
    @JsonProperty("borrowPeriod")
    private int borrowPeriod;
    @JsonProperty("extendLimit")
    private int extendLimit;
    @JsonProperty("extendPeriod")
    private int extendPeriod;
    @JsonProperty("subdomain")
    private String ssSubdomain;
    @JsonProperty("clientId")
    private String oneRosterClientId;
    @JsonProperty("clientSecret")
    private String oneRosterClientSecret;
    private List<LocationDTO> locations;
}