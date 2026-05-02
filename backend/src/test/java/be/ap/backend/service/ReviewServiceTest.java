package be.ap.backend.service;

import be.ap.backend.dto.ReviewDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Review;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
        "app.bcrypt-rounds=10",
        "app.smartschool.client-id=test",
        "app.smartschool.client-secret=test",
        "app.smartschool.callback=http://localhost:8080/oauth"
})
public class ReviewServiceTest {

    @MockitoBean
    private ReviewRepository reviewRepository;

    @MockitoBean
    private BookRepository bookRepository;

    @Autowired
    private ReviewService reviewService;

    @Test
    void givenBookWithReviews_whenGetReviews_thenReturnDTOs() {
        Book book = new Book();
        book.setId(1L);

        Review review1 = new Review();
        review1.setId(1L);
        review1.setBook(book);
        review1.setRating(4);
        review1.setContent("Goed boek!");

        Review review2 = new Review();
        review2.setId(2L);
        review2.setBook(book);
        review2.setRating(2);
        review2.setContent("Niet mijn ding.");

        when(reviewRepository.findByBookIdAndHiddenFalse(1L)).thenReturn(List.of(review1, review2));

        List<ReviewDTO> result = reviewService.getReviewsForBook(1L);

        assertEquals(2, result.size());
        assertEquals(4, result.get(0).getRating());
        assertEquals("Goed boek!", result.get(0).getContent());
        verify(reviewRepository, times(1)).findByBookIdAndHiddenFalse(1L);
    }

    @Test
    void givenValidReview_whenAddReview_thenSaveAndUpdateRating() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test boek");

        ReviewDTO dto = new ReviewDTO();
        dto.setRating(5);
        dto.setContent("Top boek!");

        Review savedReview = new Review();
        savedReview.setId(1L);
        savedReview.setBook(book);
        savedReview.setRating(5);
        savedReview.setContent("Top boek!");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
        when(reviewRepository.findAverageRatingByBookId(1L)).thenReturn(5.0);
        when(reviewRepository.findReviewCountByBookId(1L)).thenReturn(1L);

        ReviewDTO result = reviewService.addReview(1L, dto, 1L);

        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Top boek!", result.getContent());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void givenNonExistingBook_whenAddReview_thenThrowException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        ReviewDTO dto = new ReviewDTO();
        dto.setRating(3);

        assertThrows(RuntimeException.class, () -> reviewService.addReview(99L, dto, 1L));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void givenMultipleReviews_whenAddReview_thenRatingIsAverage() {
        Book book = new Book();
        book.setId(1L);

        ReviewDTO dto = new ReviewDTO();
        dto.setRating(3);

        Review savedReview = new Review();
        savedReview.setId(3L);
        savedReview.setBook(book);
        savedReview.setRating(3);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
        when(reviewRepository.findAverageRatingByBookId(1L)).thenReturn(3.5);
        when(reviewRepository.findReviewCountByBookId(1L)).thenReturn(2L);

        reviewService.addReview(1L, dto, 1L);

        verify(bookRepository).save(argThat(b -> b.getRating() == 3.5 && b.getRatingCount() == 2));
    }

    @Test
    void givenOwnReview_whenDeleteReview_thenDeleteSuccessfully() {
        Book book = new Book();
        book.setId(1L);

        Review review = new Review();
        review.setId(1L);
        review.setBook(book);
        review.setUserId(1L);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.findAverageRatingByBookId(1L)).thenReturn(4.0);
        when(reviewRepository.findReviewCountByBookId(1L)).thenReturn(1L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        reviewService.deleteReview(1L, 1L);

        verify(reviewRepository, times(1)).deleteById(1L);
    }

    @Test
    void givenOtherUsersReview_whenDeleteReview_thenThrowException() {
        Review review = new Review();
        review.setId(1L);
        review.setUserId(1L);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThrows(IllegalArgumentException.class, () -> reviewService.deleteReview(1L, 2L));
        verify(reviewRepository, never()).deleteById(any());
    }

    @Test
    void givenNonExistingReview_whenDeleteReview_thenThrowException() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> reviewService.deleteReview(99L, 1L));
        verify(reviewRepository, never()).deleteById(any());
    }
}