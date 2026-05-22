package be.ap.backend.controller;

import be.ap.backend.dto.CreateReportDTO;
import be.ap.backend.dto.ReviewDTO;
import be.ap.backend.dto.ReviewReportDTO;
import be.ap.backend.service.ReviewReportService;
import be.ap.backend.service.ReviewService;
import jakarta.servlet.http.HttpSession;
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

    @GetMapping("/book/{bookId}")
    public List<ReviewDTO> getReviewsForBook(@PathVariable Long bookId) {
        return reviewService.getReviewsForBook(bookId);
    }

    @PostMapping("/book/{bookId}")
    public ResponseEntity<ReviewDTO> addReview(@PathVariable Long bookId, @Valid @RequestBody ReviewDTO dto,
            HttpSession session) {
        Long userId = Long.valueOf(session.getAttribute("userId").toString());
        return ResponseEntity.ok(reviewService.addReview(bookId, dto, userId));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId, HttpSession session) {
        Long userId = Long.valueOf(session.getAttribute("userId").toString());
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reviewId}/rapporteer")
    public ResponseEntity<Void> reportReview(@PathVariable Long reviewId,
            @Valid @RequestBody CreateReportDTO dto, HttpSession session) {
        Long userId = Long.valueOf(session.getAttribute("userId").toString());
        reviewReportService.reportReview(reviewId, userId, dto.getNote());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rapportages")
    public ResponseEntity<List<ReviewReportDTO>> getPendingReports(HttpSession session) {
        Object rawRole = session.getAttribute("role");
        if (rawRole == null || !"BIBLIOTHEEKBEHEERDER".equals(rawRole.toString())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(reviewReportService.getPendingReports());
    }

    @PutMapping("/rapportages/{reportId}/accepteren")
    public ResponseEntity<Void> acceptReport(@PathVariable Long reportId, HttpSession session) {
        Object rawRole = session.getAttribute("role");
        if (rawRole == null || !"BIBLIOTHEEKBEHEERDER".equals(rawRole.toString())) {
            return ResponseEntity.status(403).build();
        }
        reviewReportService.acceptReport(reportId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/rapportages/{reportId}/weigeren")
    public ResponseEntity<Void> rejectReport(@PathVariable Long reportId, HttpSession session) {
        Object rawRole = session.getAttribute("role");
        if (rawRole == null || !"BIBLIOTHEEKBEHEERDER".equals(rawRole.toString())) {
            return ResponseEntity.status(403).build();
        }
        reviewReportService.rejectReport(reportId);
        return ResponseEntity.ok().build();
    }
}