package be.ap.backend.controller;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.CreateReportDTO;
import be.ap.backend.dto.ReviewDTO;
import be.ap.backend.dto.ReviewReportDTO;
import be.ap.backend.entity.ReviewReport;
import be.ap.backend.entity.UserRole;
import be.ap.backend.service.ReviewReportService;
import be.ap.backend.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebMvcTest(ReviewController.class)
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

    @MockitoBean
    private SessionContext sessionContext;

    @Autowired
    private ReviewController controller;

    private void asUser(Long userId) {
        when(sessionContext.getUserId()).thenReturn(userId);
    }

    private void asLibrarian() {
        when(sessionContext.hasRole(UserRole.BIBLIOTHEEKBEHEERDER)).thenReturn(true);
    }

    private void asNonLibrarian() {
        when(sessionContext.hasRole(UserRole.BIBLIOTHEEKBEHEERDER)).thenReturn(false);
    }

    // ── getReviewsForBook ─────────────────────────────────────────

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

    // ── addReview ─────────────────────────────────────────────────

    @Test
    void givenValidReview_whenAddReview_thenReturnSavedReview() {
        asUser(1L);

        ReviewDTO input = new ReviewDTO();
        input.setRating(5);
        input.setContent("Geweldig boek!");

        ReviewDTO saved = new ReviewDTO();
        saved.setId(1L);
        saved.setBookId(1L);
        saved.setRating(5);
        saved.setContent("Geweldig boek!");

        when(reviewService.addReview(1L, input, 1L)).thenReturn(saved);

        ReviewDTO result = (ReviewDTO) controller.addReview(1L, input).getBody();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(5, result.getRating());
        verify(reviewService, times(1)).addReview(1L, input, 1L);
    }

    @Test
    void givenReviewWithBadWord_whenAddReview_thenReturnBadRequest() {
        asUser(1L);

        ReviewDTO input = new ReviewDTO();
        input.setRating(3);
        input.setContent("Dit boek is echt kut.");

        when(reviewService.addReview(1L, input, 1L))
                .thenThrow(new IllegalArgumentException("Je recensie bevat ongepaste taal."));

        ResponseEntity<?> result = controller.addReview(1L, input);

        assertEquals(400, result.getStatusCode().value());
        assertEquals("Je recensie bevat ongepaste taal.", result.getBody());
    }

    @Test
    void givenNoSession_whenAddReview_thenReturn401() {
        when(sessionContext.getUserId()).thenReturn(null);

        ReviewDTO input = new ReviewDTO();
        input.setRating(4);
        input.setContent("Interessant.");

        assertThatThrownBy(() -> controller.addReview(1L, input))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
    }

    // ── deleteReview ──────────────────────────────────────────────

    @Test
    void givenOwnReview_whenDeleteReview_thenReturnOk() {
        asUser(1L);
        doNothing().when(reviewService).deleteReview(1L, 1L);

        ResponseEntity<?> result = controller.deleteReview(1L);

        assertEquals(200, result.getStatusCode().value());
        verify(reviewService, times(1)).deleteReview(1L, 1L);
    }

    @Test
    void givenOtherUsersReview_whenDeleteReview_thenReturnBadRequest() {
        asUser(2L);
        doThrow(new IllegalArgumentException("Je kan alleen je eigen recensie verwijderen."))
                .when(reviewService).deleteReview(1L, 2L);

        ResponseEntity<?> result = controller.deleteReview(1L);

        assertEquals(400, result.getStatusCode().value());
        assertEquals("Je kan alleen je eigen recensie verwijderen.", result.getBody());
    }

    @Test
    void givenNoSession_whenDeleteReview_thenReturn401() {
        when(sessionContext.getUserId()).thenReturn(null);

        assertThatThrownBy(() -> controller.deleteReview(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
    }

    // ── reportReview ──────────────────────────────────────────────

    @Test
    void givenValidReport_whenReportReview_thenReturnOk() {
        asUser(3L);

        CreateReportDTO dto = new CreateReportDTO();
        dto.setNote("Ongepaste inhoud");

        when(reviewReportService.reportReview(1L, 3L, "Ongepaste inhoud")).thenReturn(new ReviewReport());

        ResponseEntity<?> result = controller.reportReview(1L, dto);

        assertEquals(200, result.getStatusCode().value());
        verify(reviewReportService, times(1)).reportReview(1L, 3L, "Ongepaste inhoud");
    }

    @Test
    void givenOwnReview_whenReportReview_thenReturnBadRequest() {
        asUser(1L);

        CreateReportDTO dto = new CreateReportDTO();
        dto.setNote("eigen recensie");

        doThrow(new IllegalArgumentException("Je kan je eigen recensie niet rapporteren."))
                .when(reviewReportService).reportReview(1L, 1L, "eigen recensie");

        ResponseEntity<?> result = controller.reportReview(1L, dto);

        assertEquals(400, result.getStatusCode().value());
        assertEquals("Je kan je eigen recensie niet rapporteren.", result.getBody());
    }

    @Test
    void givenNoSession_whenReportReview_thenReturn401() {
        when(sessionContext.getUserId()).thenReturn(null);

        CreateReportDTO dto = new CreateReportDTO();
        dto.setNote("Ongepaste inhoud");

        assertThatThrownBy(() -> controller.reportReview(1L, dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
    }

    // ── getPendingReports ─────────────────────────────────────────

    @Test
    void givenLibrarian_whenGetPendingReports_thenReturnReports() {
        asLibrarian();

        ReviewReportDTO report = new ReviewReportDTO();
        report.setId(1L);
        report.setNote("Spam");

        when(reviewReportService.getPendingReports()).thenReturn(List.of(report));

        ResponseEntity<List<ReviewReportDTO>> result = controller.getPendingReports();

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        verify(reviewReportService, times(1)).getPendingReports();
    }

    @Test
    void givenNonLibrarian_whenGetPendingReports_thenReturn403() {
        asNonLibrarian();

        assertThatThrownBy(() -> controller.getPendingReports())
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");

        verify(reviewReportService, never()).getPendingReports();
    }

    // ── acceptReport ──────────────────────────────────────────────

    @Test
    void givenLibrarian_whenAcceptReport_thenReturnOk() {
        asLibrarian();
        doNothing().when(reviewReportService).acceptReport(1L);

        ResponseEntity<Void> result = controller.acceptReport(1L);

        assertEquals(200, result.getStatusCode().value());
        verify(reviewReportService, times(1)).acceptReport(1L);
    }

    @Test
    void givenNonLibrarian_whenAcceptReport_thenReturn403() {
        asNonLibrarian();

        assertThatThrownBy(() -> controller.acceptReport(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");

        verify(reviewReportService, never()).acceptReport(any());
    }

    // ── rejectReport ──────────────────────────────────────────────

    @Test
    void givenLibrarian_whenRejectReport_thenReturnOk() {
        asLibrarian();
        doNothing().when(reviewReportService).rejectReport(1L);

        ResponseEntity<Void> result = controller.rejectReport(1L);

        assertEquals(200, result.getStatusCode().value());
        verify(reviewReportService, times(1)).rejectReport(1L);
    }

    @Test
    void givenNonLibrarian_whenRejectReport_thenReturn403() {
        asNonLibrarian();

        assertThatThrownBy(() -> controller.rejectReport(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");

        verify(reviewReportService, never()).rejectReport(any());
    }
}