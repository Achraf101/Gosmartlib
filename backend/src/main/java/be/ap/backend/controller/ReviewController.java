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

@RestController
@RequestMapping("review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewReportService reviewReportService;
    private final SessionContext sessionContext;

    @GetMapping("/book/{bookId}")
    public List<ReviewDTO> getReviewsForBook(@PathVariable Long bookId) {
        return reviewService.getReviewsForBook(bookId);
    }

    @PostMapping("/book/{bookId}")
    public ResponseEntity<ReviewDTO> addReview(@PathVariable Long bookId, @Valid @RequestBody ReviewDTO dto) {
        Long userId = requireUserId();
        return ResponseEntity.ok(reviewService.addReview(bookId, dto, userId));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        Long userId = requireUserId();
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reviewId}/rapporteer")
    public ResponseEntity<Void> reportReview(@PathVariable Long reviewId,
            @Valid @RequestBody CreateReportDTO dto) {
        Long userId = requireUserId();
        reviewReportService.reportReview(reviewId, userId, dto.getNote());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rapportages")
    public ResponseEntity<List<ReviewReportDTO>> getPendingReports() {
        requireRole(UserRole.BIBLIOTHEEKBEHEERDER);
        return ResponseEntity.ok(reviewReportService.getPendingReports());
    }

    @PutMapping("/rapportages/{reportId}/accepteren")
    public ResponseEntity<Void> acceptReport(@PathVariable Long reportId) {
        requireRole(UserRole.BIBLIOTHEEKBEHEERDER);
        reviewReportService.acceptReport(reportId);
        return ResponseEntity.ok().build();
    }

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