package be.ap.backend.dto;

import lombok.Data;

@Data
public class CampusBookDetailDTO {
    private Long id;
    private Long campusId;
    private Long bookId;
    private String bookTitle;
    private String authorName;
    private String bookCover;
    private Integer amount;
    private Integer currentAmount;
    private String location;
}