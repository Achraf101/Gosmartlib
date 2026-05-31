package be.ap.backend.service;

import be.ap.backend.dto.ReviewDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Review;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing book reviews, including content filtering and rating
 * updates.
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final FilterService filterService;

    public List<ReviewDTO> getReviewsForBook(Long bookId) {
        return reviewRepository.findByBookIdAndHiddenFalse(bookId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Adds a review for the given book after validating content for bad words and
     * URLs.
     * Updates the book's aggregate rating after saving.
     *
     * @throws EntityNotFoundException  if the book does not exist
     * @throws IllegalArgumentException if the review content contains inappropriate
     *                                  language or URLs
     */
    public ReviewDTO addReview(Long bookId, ReviewDTO dto, Long userId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Boek niet gevonden"));

        Review review = new Review();
        review.setBook(book);
        review.setUserId(userId);
        review.setRating(dto.getRating());
        if (filterService.containsBadWord(dto.getContent())) {
            throw new IllegalArgumentException("Je recensie bevat ongepaste taal.");
        }
        if (filterService.containsUrl(dto.getContent())) {
            throw new IllegalArgumentException("Je recensie mag geen URLs bevatten.");
        }
        review.setContent(dto.getContent());

        Review saved = reviewRepository.save(review);

        updateBookRating(bookId);

        return toDTO(saved);
    }

    /**
     * Deletes the given review if it belongs to the requesting user.
     * Updates the book's aggregate rating after deletion.
     *
     * @throws IllegalArgumentException if the review does not exist or does not
     *                                  belong to the user
     */
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Recensie niet gevonden."));
        if (!review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Je kan alleen je eigen recensie verwijderen.");
        }
        reviewRepository.deleteById(reviewId);
        updateBookRating(review.getBook().getId());
    }

    /**
     * Deletes the given review without ownership checks, for use by moderators.
     * Updates the book's aggregate rating after deletion.
     *
     * @throws IllegalArgumentException if the review does not exist
     */
    public void forceDeleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Recensie niet gevonden."));
        Long bookId = review.getBook().getId();
        reviewRepository.deleteById(reviewId);
        updateBookRating(bookId);
    }

    /**
     * Recalculates and persists the average rating and review count for the given
     * book.
     */
    void updateBookRating(Long bookId) {
        Double avg = reviewRepository.findAverageRatingByBookId(bookId);
        Long count = reviewRepository.findReviewCountByBookId(bookId);

        Book book = bookRepository.findById(bookId).orElseThrow();
        book.setRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0);
        book.setRatingCount(count != null ? count.intValue() : 0);
        bookRepository.save(book);
    }

    private ReviewDTO toDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setBookId(review.getBook().getId());
        dto.setRating(review.getRating());
        dto.setContent(review.getContent());
        dto.setAdded(review.getAdded());
        dto.setUserId(review.getUserId());
        return dto;
    }
}
