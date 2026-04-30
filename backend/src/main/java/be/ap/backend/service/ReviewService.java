package be.ap.backend.service;

import be.ap.backend.dto.ReviewDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Review;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    public ReviewDTO addReview(Long bookId, ReviewDTO dto) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Boek niet gevonden"));

        Review review = new Review();
        review.setBook(book);
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

    private void updateBookRating(Long bookId) {
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
        return dto;
    }
}