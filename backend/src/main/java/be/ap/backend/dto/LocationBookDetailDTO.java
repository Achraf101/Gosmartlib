package be.ap.backend.dto;

import lombok.Data;

@Data
public class LocationBookDetailDTO {
    private Long id;
    private Long locationId;
    private Long bookId;
    private String bookTitle;
    private String authorName;
    private String bookCover;
    private Integer amount;
    private Integer currentAmount;
}