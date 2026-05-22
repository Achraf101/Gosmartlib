package be.ap.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CurrentUserDTO {
    @JsonProperty("userId")
    private Long userId;
    private String role;
    @JsonProperty("schoolId")
    private Long schoolId;
    @JsonProperty("locationId")
    private Long locationId;
    @JsonProperty("firstName")
    private String firstName;
    @JsonProperty("lastName")
    private String lastName;
    private String email;
    private String username;
}