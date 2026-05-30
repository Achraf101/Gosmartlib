package be.ap.backend.dto;

import be.ap.backend.enums.CopyStatus;
import lombok.Data;

@Data
public class BookCopyDetailDTO {
    private Long id;
    private String accessionId;
    private CopyStatus status;
    private Long locationBookId;
    private Long bookId;
    private String bookTitle;
    private String authorName;
    private String bookCover;
    private String isbn;
    private Long locationId;
    private String locationName;
    private Integer amount;
    private Integer currentAmount;
    private String note;
}
