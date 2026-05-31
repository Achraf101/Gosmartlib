package be.ap.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object representing a classroom overview.
 *
 * <p>
 * Used to expose basic classroom information including its identifier,
 * name, and the number of enrolled students.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomDTO {
    private Long id;
    private String name;
    @JsonProperty("studentCount")
    private int studentCount;
}
