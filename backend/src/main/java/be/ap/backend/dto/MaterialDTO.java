package be.ap.backend.dto;

import java.time.LocalDateTime;

public class MaterialDTO {
    public Long bookId;
    public String fileId;
    public String comment;
    public String fileName;
    public Long size;
    public LocalDateTime uploaded;

    public MaterialDTO(Long bookId, String fileId, String fileName, Long size, LocalDateTime uploaded) {
        this.bookId = bookId;
        this.fileId = fileId;
        this.fileName = fileName;
        this.size = size;
        this.uploaded = uploaded;
    }
}
