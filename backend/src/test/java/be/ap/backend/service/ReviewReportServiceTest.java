package be.ap.backend.service;

import be.ap.backend.dto.ReviewReportDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Review;
import be.ap.backend.entity.ReviewReport;
import be.ap.backend.entity.User;
import be.ap.backend.entity.UserRole;
import be.ap.backend.enums.ReviewReportStatus;
import be.ap.backend.repository.ReviewReportRepository;
import be.ap.backend.repository.ReviewRepository;
import be.ap.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@WebMvcTest(ReviewReportService.class)
@TestPropertySource(properties = {
        "app.bcrypt-rounds=10",
        "app.smartschool.client-id=test",
        "app.smartschool.client-secret=test",
        "app.smartschool.callback=http://localhost:8080/oauth"
})
public class ReviewReportServiceTest {

    @MockitoBean
    private ReviewReportRepository reviewReportRepository;

    @MockitoBean
    private ReviewRepository reviewRepository;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private SmartschoolLookupService lookupService;

    @Autowired
    private ReviewReportService reviewReportService;

    @Test
    void givenValidReport_whenReportReview_thenSaveReport() {
        Book book = new Book();
        book.setId(1L);

        Review review = new Review();
        review.setId(1L);
        review.setBook(book);
        review.setUserId(2L);

        ReviewReport saved = new ReviewReport();
        saved.setId(1L);
        saved.setReviewId(1L);
        saved.setReporterUserId(3L);
        saved.setNote("Ongepaste inhoud");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewReportRepository.existsByReviewIdAndReporterUserId(1L, 3L)).thenReturn(false);
        when(reviewReportRepository.save(any(ReviewReport.class))).thenReturn(saved);

        ReviewReport result = reviewReportService.reportReview(1L, 3L, "Ongepaste inhoud");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Ongepaste inhoud", result.getNote());
        verify(reviewReportRepository, times(1)).save(any(ReviewReport.class));
    }

    @Test
    void givenOwnReview_whenReportReview_thenThrowException() {
        Review review = new Review();
        review.setId(1L);
        review.setUserId(1L);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThrows(IllegalArgumentException.class, () -> reviewReportService.reportReview(1L, 1L, "test"));
        verify(reviewReportRepository, never()).save(any());
    }

    @Test
    void givenDuplicateReport_whenReportReview_thenThrowException() {
        Review review = new Review();
        review.setId(1L);
        review.setUserId(2L);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewReportRepository.existsByReviewIdAndReporterUserId(1L, 3L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> reviewReportService.reportReview(1L, 3L, "dubbel"));
        verify(reviewReportRepository, never()).save(any());
    }

    @Test
    void givenNonExistingReview_whenReportReview_thenThrowException() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> reviewReportService.reportReview(99L, 1L, "test"));
        verify(reviewReportRepository, never()).save(any());
    }

    @Test
    void givenPendingReports_whenGetPendingReports_thenReturnDTOsWithUsernames() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test boek");

        Review review = new Review();
        review.setId(1L);
        review.setBook(book);
        review.setRating(3);
        review.setContent("Slechte recensie");
        review.setAdded(LocalDateTime.now());
        review.setUserId(2L);

        ReviewReport report = new ReviewReport();
        report.setId(1L);
        report.setReviewId(1L);
        report.setReporterUserId(3L);
        report.setNote("Ongepast");
        report.setCreatedAt(LocalDateTime.now());
        report.setStatus(ReviewReportStatus.PENDING);

        User reviewer = new User("jan_leerling", null, UserRole.STUDENT);
        User reporter = new User("piet_rapporteur", null, UserRole.STUDENT);

        when(reviewReportRepository.findByStatus(ReviewReportStatus.PENDING)).thenReturn(List.of(report));
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(userRepository.findById(2L)).thenReturn(Optional.of(reviewer));
        when(userRepository.findById(3L)).thenReturn(Optional.of(reporter));
        when(lookupService.getUser(any(), any(), any()))
                .thenReturn(Map.of("givenName", "Jan", "familyName", "Doe"));

        List<ReviewReportDTO> result = reviewReportService.getPendingReports();

        assertEquals(1, result.size());
        ReviewReportDTO dto = result.get(0);
        assertEquals(1L, dto.getId());
        assertEquals("Test boek", dto.getBookTitle());
        assertEquals("Slechte recensie", dto.getReviewContent());
        assertEquals("jan_leerling", dto.getReviewUsername());
        assertEquals("piet_rapporteur", dto.getReporterUsername());
        verify(reviewReportRepository, times(1)).findByStatus(ReviewReportStatus.PENDING);
    }

    @Test
    void givenNoPendingReports_whenGetPendingReports_thenReturnEmptyList() {
        when(reviewReportRepository.findByStatus(ReviewReportStatus.PENDING)).thenReturn(List.of());

        List<ReviewReportDTO> result = reviewReportService.getPendingReports();

        assertEquals(0, result.size());
    }

    @Test
    void givenValidReport_whenAcceptReport_thenForceDeleteReviewAndSetAccepted() {
        ReviewReport report = new ReviewReport();
        report.setId(1L);
        report.setReviewId(5L);
        report.setStatus(ReviewReportStatus.PENDING);

        when(reviewReportRepository.findById(1L)).thenReturn(Optional.of(report));
        doNothing().when(reviewService).forceDeleteReview(5L);
        when(reviewReportRepository.save(any(ReviewReport.class))).thenReturn(report);

        reviewReportService.acceptReport(1L);

        verify(reviewService, times(1)).forceDeleteReview(5L);
        verify(reviewReportRepository, times(1)).save(argThat(r -> r.getStatus() == ReviewReportStatus.ACCEPTED));
    }

    @Test
    void givenNonExistingReport_whenAcceptReport_thenThrowException() {
        when(reviewReportRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> reviewReportService.acceptReport(99L));
        verify(reviewService, never()).forceDeleteReview(any());
    }

    @Test
    void givenValidReport_whenRejectReport_thenSetRejected() {
        ReviewReport report = new ReviewReport();
        report.setId(1L);
        report.setStatus(ReviewReportStatus.PENDING);

        when(reviewReportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(reviewReportRepository.save(any(ReviewReport.class))).thenReturn(report);

        reviewReportService.rejectReport(1L);

        verify(reviewReportRepository, times(1)).save(argThat(r -> r.getStatus() == ReviewReportStatus.REJECTED));
    }

    @Test
    void givenNonExistingReport_whenRejectReport_thenThrowException() {
        when(reviewReportRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> reviewReportService.rejectReport(99L));
        verify(reviewReportRepository, never()).save(any());
    }
}