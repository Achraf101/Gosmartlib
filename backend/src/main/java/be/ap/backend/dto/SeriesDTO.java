package be.ap.backend.dto;

import lombok.Data;

/**
 * DTO representing a book series and its author.
 */
@Data
public class SeriesDTO {
    private Long id;
    private String name;
    private String description;
    private Long authorId;
    private String authorName;
}