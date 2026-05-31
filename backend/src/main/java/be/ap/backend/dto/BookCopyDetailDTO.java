package be.ap.backend.dto;

import be.ap.backend.enums.CopyStatus;
import lombok.Data;

/**
 * Detailed data transfer object representing a book copy in a specific
 * location.
 *
 * <p>
 * Used to expose enriched information about a physical book copy,
 * including book metadata, location data, and availability status.
 * </p>
 */
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
