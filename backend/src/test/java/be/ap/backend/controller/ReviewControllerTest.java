package be.ap.backend.controller;

import be.ap.backend.dto.ReviewDTO;
import be.ap.backend.dto.ReviewReportDTO;
import be.ap.backend.service.ReviewReportService;
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

    @MockitoBean
    private ReviewReportService reviewReportService;

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

    @Test
    void givenValidReport_whenReportReview_thenReturnOk() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "3");

        be.ap.backend.dto.CreateReportDTO dto = new be.ap.backend.dto.CreateReportDTO();
        dto.setNote("Ongepaste inhoud");

        doNothing().when(reviewReportService).reportReview(1L, 3L, "Ongepaste inhoud");

        ResponseEntity<?> result = controller.reportReview(1L, dto, session);

        assertEquals(200, result.getStatusCode().value());
        verify(reviewReportService, times(1)).reportReview(1L, 3L, "Ongepaste inhoud");
    }

    @Test
    void givenOwnReview_whenReportReview_thenReturnBadRequest() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "1");

        be.ap.backend.dto.CreateReportDTO dto = new be.ap.backend.dto.CreateReportDTO();
        dto.setNote("eigen recensie");

        doThrow(new IllegalArgumentException("Je kan je eigen recensie niet rapporteren."))
                .when(reviewReportService).reportReview(1L, 1L, "eigen recensie");

        ResponseEntity<?> result = controller.reportReview(1L, dto, session);

        assertEquals(400, result.getStatusCode().value());
        assertEquals("Je kan je eigen recensie niet rapporteren.", result.getBody());
    }

    @Test
    void givenLibrarian_whenGetPendingReports_thenReturnReports() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "BIBLIOTHEEKBEHEERDER");

        ReviewReportDTO report = new ReviewReportDTO();
        report.setId(1L);
        report.setNote("Spam");

        when(reviewReportService.getPendingReports()).thenReturn(List.of(report));

        ResponseEntity<?> result = controller.getPendingReports(session);

        assertEquals(200, result.getStatusCode().value());
        List<?> body = (List<?>) result.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        verify(reviewReportService, times(1)).getPendingReports();
    }

    @Test
    void givenNonLibrarian_whenGetPendingReports_thenReturn403() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "STUDENT");

        ResponseEntity<?> result = controller.getPendingReports(session);

        assertEquals(403, result.getStatusCode().value());
        verify(reviewReportService, never()).getPendingReports();
    }

    @Test
    void givenLibrarian_whenAcceptReport_thenReturnOk() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "BIBLIOTHEEKBEHEERDER");

        doNothing().when(reviewReportService).acceptReport(1L);

        ResponseEntity<?> result = controller.acceptReport(1L, session);

        assertEquals(200, result.getStatusCode().value());
        verify(reviewReportService, times(1)).acceptReport(1L);
    }

    @Test
    void givenLibrarian_whenRejectReport_thenReturnOk() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "BIBLIOTHEEKBEHEERDER");

        doNothing().when(reviewReportService).rejectReport(1L);

        ResponseEntity<?> result = controller.rejectReport(1L, session);

        assertEquals(200, result.getStatusCode().value());
        verify(reviewReportService, times(1)).rejectReport(1L);
    }

    @Test
    void givenNonLibrarian_whenAcceptReport_thenReturn403() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "LEERKRACHT");

        ResponseEntity<?> result = controller.acceptReport(1L, session);

        assertEquals(403, result.getStatusCode().value());
        verify(reviewReportService, never()).acceptReport(any());
    }
}