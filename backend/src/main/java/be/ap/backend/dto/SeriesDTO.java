package be.ap.backend.dto;

import lombok.Data;

@Data
public class SeriesDTO {
    private Long id;
    private String name;
    private String description;
    private Long authorId;
    private String authorName;
}