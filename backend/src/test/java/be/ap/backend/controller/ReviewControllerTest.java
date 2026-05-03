package be.ap.backend.controller;

import be.ap.backend.dto.ReviewDTO;
import be.ap.backend.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
        "app.bcrypt-rounds=10",
        "app.smartschool.client-id=test",
        "app.smartschool.client-secret=test",
        "app.smartschool.callback=http://localhost:8080/oauth"
})
public class ReviewControllerTest {

    @MockitoBean
    private ReviewService reviewService;

    @Autowired
    private ReviewController controller;

    @Test
    void givenBookWithReviews_whenGetReviews_thenReturnReviews() {
        ReviewDTO review1 = new ReviewDTO();
        review1.setId(1L);
        review1.setBookId(1L);
        review1.setRating(4);
        review1.setContent("Goed boek!");

        ReviewDTO review2 = new ReviewDTO();
        review2.setId(2L);
        review2.setBookId(1L);
        review2.setRating(2);
        review2.setContent("Niet mijn ding.");

        when(reviewService.getReviewsForBook(1L)).thenReturn(List.of(review1, review2));

        List<ReviewDTO> result = controller.getReviewsForBook(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(4, result.get(0).getRating());
        assertEquals("Goed boek!", result.get(0).getContent());
        verify(reviewService, times(1)).getReviewsForBook(1L);
    }

    @Test
    void givenNoReviews_whenGetReviews_thenReturnEmptyList() {
        when(reviewService.getReviewsForBook(99L)).thenReturn(List.of());

        List<ReviewDTO> result = controller.getReviewsForBook(99L);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(reviewService, times(1)).getReviewsForBook(99L);
    }

    @Test
    void givenValidReview_whenAddReview_thenReturnSavedReview() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "1");

        ReviewDTO input = new ReviewDTO();
        input.setRating(5);
        input.setContent("Geweldig boek!");

        ReviewDTO saved = new ReviewDTO();
        saved.setId(1L);
        saved.setBookId(1L);
        saved.setRating(5);
        saved.setContent("Geweldig boek!");

        when(reviewService.addReview(1L, input, 1L)).thenReturn(saved);

        ReviewDTO result = (ReviewDTO) controller.addReview(1L, input, session).getBody();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(5, result.getRating());
        assertEquals("Geweldig boek!", result.getContent());
        verify(reviewService, times(1)).addReview(1L, input, 1L);
    }

    @Test
    void givenReviewWithoutContent_whenAddReview_thenReturnReview() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "1");

        ReviewDTO input = new ReviewDTO();
        input.setRating(3);

        ReviewDTO saved = new ReviewDTO();
        saved.setId(2L);
        saved.setBookId(1L);
        saved.setRating(3);

        when(reviewService.addReview(1L, input, 1L)).thenReturn(saved);

        ReviewDTO result = (ReviewDTO) controller.addReview(1L, input, session).getBody();

        assertNotNull(result);
        assertEquals(3, result.getRating());
        assertNull(result.getContent());
        verify(reviewService, times(1)).addReview(1L, input, 1L);
    }

    @Test
    void givenReviewWithBadWord_whenAddReview_thenReturnBadRequest() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "1");

        ReviewDTO input = new ReviewDTO();
        input.setRating(3);
        input.setContent("Dit boek is echt kut.");

        when(reviewService.addReview(1L, input, 1L))
                .thenThrow(new IllegalArgumentException("Je recensie bevat ongepaste taal."));

        ResponseEntity<?> result = controller.addReview(1L, input, session);

        assertEquals(400, result.getStatusCode().value());
        assertEquals("Je recensie bevat ongepaste taal.", result.getBody());
        verify(reviewService, times(1)).addReview(1L, input, 1L);
    }

    @Test
    void givenReviewWithUrl_whenAddReview_thenReturnBadRequest() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "1");

        ReviewDTO input = new ReviewDTO();
        input.setRating(3);
        input.setContent("Kijk op www.spam.com voor meer info.");

        when(reviewService.addReview(1L, input, 1L))
                .thenThrow(new IllegalArgumentException("Je recensie mag geen URLs bevatten."));

        ResponseEntity<?> result = controller.addReview(1L, input, session);

        assertEquals(400, result.getStatusCode().value());
        assertEquals("Je recensie mag geen URLs bevatten.", result.getBody());
        verify(reviewService, times(1)).addReview(1L, input, 1L);
    }

    @Test
    void givenOwnReview_whenDeleteReview_thenReturnOk() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "1");

        doNothing().when(reviewService).deleteReview(1L, 1L);

        ResponseEntity<?> result = controller.deleteReview(1L, session);

        assertEquals(200, result.getStatusCode().value());
        verify(reviewService, times(1)).deleteReview(1L, 1L);
    }

    @Test
    void givenOtherUsersReview_whenDeleteReview_thenReturnBadRequest() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "2");

        doThrow(new IllegalArgumentException("Je kan alleen je eigen recensie verwijderen."))
                .when(reviewService).deleteReview(1L, 2L);

        ResponseEntity<?> result = controller.deleteReview(1L, session);

        assertEquals(400, result.getStatusCode().value());
        assertEquals("Je kan alleen je eigen recensie verwijderen.", result.getBody());
        verify(reviewService, times(1)).deleteReview(1L, 2L);
    }
}