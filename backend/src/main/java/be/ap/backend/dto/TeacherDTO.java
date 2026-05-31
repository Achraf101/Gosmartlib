package be.ap.backend.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO representing a teacher with their basic identity details.
 */
@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class TeacherDTO {
    Long userId;
    String givenName;
    String familyName;
}
