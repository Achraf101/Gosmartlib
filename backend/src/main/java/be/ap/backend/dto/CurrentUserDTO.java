package be.ap.backend.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Data Transfer Object representing the currently authenticated user.
 * <p>
 * This object is used to expose essential user information to the frontend
 * or other services without exposing the full internal user entity.
 * </p>
 */
@Data
public class CurrentUserDTO {
    @JsonProperty("userId")
    private Long userId;
    private Set<String> roles;
    @JsonProperty("schoolId")
    private Long schoolId;
    @JsonProperty("firstName")
    private String firstName;
    @JsonProperty("lastName")
    private String lastName;
    private String email;
    private String username;
}