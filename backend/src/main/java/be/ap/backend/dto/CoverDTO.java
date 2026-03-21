package be.ap.backend.dto;

import lombok.Data;

@Data
public class CoverDTO {
    public Long bookId;
    public String cover;

    public CoverDTO(Long bookId, String cover) {
        this.bookId = bookId;
        this.cover = cover;
    }
}
