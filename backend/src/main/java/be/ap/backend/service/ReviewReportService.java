package be.ap.backend.service;

import be.ap.backend.dto.ReviewReportDTO;
import be.ap.backend.entity.ReviewReport;
import be.ap.backend.entity.Review;
import be.ap.backend.enums.ReviewReportStatus;
import be.ap.backend.repository.ReviewReportRepository;
import be.ap.backend.repository.ReviewRepository;
import be.ap.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing review reports, including submission and moderation.
 */
@Service
@RequiredArgsConstructor
public class ReviewReportService {

    private final ReviewReportRepository reviewReportRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;
    private final UserRepository userRepository;
    private final SmartschoolLookupService lookupService;

    /**
     * Files a report against the given review.
     *
     * @throws IllegalArgumentException if the review does not exist, the user is
     *                                  reporting their own review,
     *                                  or the user has already reported this review
     */
    public ReviewReport reportReview(Long reviewId, Long userId, String note) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Recensie niet gevonden."));
        if (review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Je kan je eigen recensie niet rapporteren.");
        }
        if (reviewReportRepository.existsByReviewIdAndReporterUserId(reviewId, userId)) {
            throw new IllegalArgumentException("Je hebt deze recensie al gerapporteerd.");
        }
        ReviewReport report = new ReviewReport();
        report.setReviewId(reviewId);
        report.setReporterUserId(userId);
        report.setNote(note);
        return reviewReportRepository.save(report);
    }

    public List<ReviewReportDTO> getPendingReports() {
        return reviewReportRepository.findByStatus(ReviewReportStatus.PENDING).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Accepts the report and force-deletes the associated review.
     *
     * @throws IllegalArgumentException if no report exists with the given ID
     */
    public void acceptReport(Long reportId) {
        ReviewReport report = reviewReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Rapportage niet gevonden."));
        reviewService.forceDeleteReview(report.getReviewId());
        report.setStatus(ReviewReportStatus.ACCEPTED);
        reviewReportRepository.save(report);
    }

    /**
     * @throws IllegalArgumentException if no report exists with the given ID
     */
    public void rejectReport(Long reportId) {
        ReviewReport report = reviewReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Rapportage niet gevonden."));
        report.setStatus(ReviewReportStatus.REJECTED);
        reviewReportRepository.save(report);
    }

    private ReviewReportDTO toDTO(ReviewReport report) {
        ReviewReportDTO dto = new ReviewReportDTO();
        dto.setId(report.getId());
        dto.setReviewId(report.getReviewId());
        dto.setReporterUserId(report.getReporterUserId());
        dto.setNote(report.getNote());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setStatus(report.getStatus());

        reviewRepository.findById(report.getReviewId()).ifPresent(review -> {

            dto.setReviewRating(review.getRating());
            dto.setReviewContent(review.getContent());
            dto.setReviewAdded(review.getAdded());
            dto.setReviewUserId(review.getUserId());
            dto.setBookId(review.getBook().getId());
            dto.setBookTitle(review.getBook().getTitle());
            userRepository.findById(review.getUserId())
                    .ifPresent(u -> {
                        Map<String, Object> user = lookupService.getUser(u.getSchool().getId(), u.getOneRosterId(),
                                u.getRoles());
                        String firstName = (String) user.get("givenName");
                        String lastName = (String) user.get("familyName");
                        dto.setReviewUsername(u.getUsername() != null ? u.getUsername() : firstName + " " + lastName);
                    });
        });

        userRepository.findById(report.getReporterUserId())
                .ifPresent(u -> {
                    Map<String, Object> user = lookupService.getUser(u.getSchool().getId(), u.getOneRosterId(),
                            u.getRoles());
                    String firstName = (String) user.get("givenName");
                    String lastName = (String) user.get("familyName");
                    dto.setReporterUsername(u.getUsername() != null ? u.getUsername() : firstName + " " + lastName);
                });

        return dto;
    }
}
