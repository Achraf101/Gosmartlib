package be.ap.backend.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a brief student summary with their most recent activity
 * date.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentPreviewDTO {
    private Long id;
    @JsonProperty("firstName")
    private String firstName;
    @JsonProperty("lastName")
    private String lastName;
    @JsonProperty("lastActivity")
    private LocalDate lastActivity;
}