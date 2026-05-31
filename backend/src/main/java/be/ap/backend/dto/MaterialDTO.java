package be.ap.backend.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * DTO representing a supplementary material file attached to a book.
 */
@Data
public class MaterialDTO {
    private Long bookId;
    private String fileId;
    private String comment;
    private String fileName;
    private Long size;
    private LocalDateTime uploaded;
}