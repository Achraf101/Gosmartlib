package be.ap.backend.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentPreviewDTO {
    private Long id;
    private String username;
    private String name;
    @JsonProperty("lastActivity")
    private LocalDate lastActivity;
}
