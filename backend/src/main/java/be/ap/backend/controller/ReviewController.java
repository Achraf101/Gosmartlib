package be.ap.backend.controller;

import be.ap.backend.dto.ReviewDTO;
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

    @GetMapping("/book/{bookId}")
    public List<ReviewDTO> getReviewsForBook(@PathVariable Long bookId) {
        return reviewService.getReviewsForBook(bookId);
    }

    @PostMapping("/book/{bookId}")
    public ResponseEntity<?> addReview(@PathVariable Long bookId, @Valid @RequestBody ReviewDTO dto,
            HttpSession session) {
        Object raw = session.getAttribute("userId");
        Long userId = (raw != null) ? Long.valueOf(raw.toString()) : null;
        try {
            return ResponseEntity.ok(reviewService.addReview(bookId, dto, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId, HttpSession session) {
        Object raw = session.getAttribute("userId");
        Long userId = (raw != null) ? Long.valueOf(raw.toString()) : null;
        try {
            reviewService.deleteReview(reviewId, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}