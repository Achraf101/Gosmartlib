package be.ap.backend.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MaterialDTO {
    private Long bookId;
    private String fileId;
    private String comment;
    private String fileName;
    private Long size;
    private LocalDateTime uploaded;
}