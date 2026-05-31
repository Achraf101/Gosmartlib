package be.ap.backend.controller;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.CreateReportDTO;
import be.ap.backend.dto.ReviewDTO;
import be.ap.backend.dto.ReviewReportDTO;
import be.ap.backend.entity.UserRole;
import be.ap.backend.exception.MissingSessionException;
import be.ap.backend.exception.UnauthorizedAccessException;
import be.ap.backend.service.ReviewReportService;
import be.ap.backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing book reviews and review reports.
 * <p>
 * Supports creating, retrieving, deleting reviews, and moderating reported
 * reviews.
 * Access control is enforced via session-based authentication and role checks.
 * </p>
 */
@RestController
@RequestMapping("review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewReportService reviewReportService;
    private final SessionContext sessionContext;

    /**
     * Retrieves all reviews for a specific book.
     *
     * @param bookId the ID of the book
     * @return list of reviews for the book
     */
    @GetMapping("/book/{bookId}")
    public List<ReviewDTO> getReviewsForBook(@PathVariable Long bookId) {
        return reviewService.getReviewsForBook(bookId);
    }

    /**
     * Creates a new review for a book.
     *
     * @param bookId the ID of the book being reviewed
     * @param dto    the review data
     * @return the created review
     */
    @PostMapping("/book/{bookId}")
    public ResponseEntity<ReviewDTO> addReview(@PathVariable Long bookId, @Valid @RequestBody ReviewDTO dto) {
        Long userId = requireUserId();
        return ResponseEntity.ok(reviewService.addReview(bookId, dto, userId));
    }

    /**
     * Deletes a review created by the current user.
     *
     * @param reviewId the ID of the review to delete
     * @return empty response on success
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        Long userId = requireUserId();
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Reports a review as inappropriate.
     *
     * @param reviewId the ID of the review being reported
     * @param dto      report payload containing the reason/note
     * @return empty response on success
     */
    @PostMapping("/{reviewId}/rapporteer")
    public ResponseEntity<Void> reportReview(@PathVariable Long reviewId,
            @Valid @RequestBody CreateReportDTO dto) {
        Long userId = requireUserId();
        reviewReportService.reportReview(reviewId, userId, dto.getNote());
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves all pending review reports for moderation.
     *
     * @return list of pending reports
     */
    @GetMapping("/rapportages")
    public ResponseEntity<List<ReviewReportDTO>> getPendingReports() {
        requireRole(UserRole.BIBLIOTHEEKBEHEERDER);
        return ResponseEntity.ok(reviewReportService.getPendingReports());
    }

    /**
     * Accepts a review report and applies moderation actions.
     *
     * @param reportId the ID of the report to accept
     */
    @PutMapping("/rapportages/{reportId}/accepteren")
    public ResponseEntity<Void> acceptReport(@PathVariable Long reportId) {
        requireRole(UserRole.BIBLIOTHEEKBEHEERDER);
        reviewReportService.acceptReport(reportId);
        return ResponseEntity.ok().build();
    }

    /**
     * Rejects a review report without taking action.
     *
     * @param reportId the ID of the report to reject
     */
    @PutMapping("/rapportages/{reportId}/weigeren")
    public ResponseEntity<Void> rejectReport(@PathVariable Long reportId) {
        requireRole(UserRole.BIBLIOTHEEKBEHEERDER);
        reviewReportService.rejectReport(reportId);
        return ResponseEntity.ok().build();
    }

    private Long requireUserId() {
        Long userId = sessionContext.getUserId();
        if (userId == null) {
            throw new MissingSessionException("Niet ingelogd.");
        }
        return userId;
    }

    private void requireRole(UserRole role) {
        if (!sessionContext.hasRole(role)) {
            throw new UnauthorizedAccessException("Toegang geweigerd.");
        }
    }
}