package be.ap.backend.controller;

import be.ap.backend.dto.ReviewDTO;
import be.ap.backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ReviewDTO addReview(@PathVariable Long bookId, @Valid @RequestBody ReviewDTO dto) {
        return reviewService.addReview(bookId, dto);
    }
}