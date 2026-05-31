package be.ap.backend.dto;

import be.ap.backend.enums.ReviewReportStatus;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO representing a report filed against a review, including reporter details,
 * the reported review's content, and the associated book.
 */
@Data
public class ReviewReportDTO {
    private Long id;
    private Long reviewId;
    private Long reporterUserId;
    private String note;
    private LocalDateTime createdAt;
    private ReviewReportStatus status;

    private double reviewRating;
    private String reviewContent;
    private LocalDateTime reviewAdded;
    private Long reviewUserId;

    private Long bookId;
    private String bookTitle;

    private String reviewUsername;
    private String reporterUsername;
}
