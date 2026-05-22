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

@Service
@RequiredArgsConstructor
public class ReviewReportService {

    private final ReviewReportRepository reviewReportRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;
    private final UserRepository userRepository;
    private final SmartschoolLookupService lookupService;

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

    public void acceptReport(Long reportId) {
        ReviewReport report = reviewReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Rapportage niet gevonden."));
        reviewService.forceDeleteReview(report.getReviewId());
        report.setStatus(ReviewReportStatus.ACCEPTED);
        reviewReportRepository.save(report);
    }

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
                        Map<String, Object> user = lookupService.getUser(u.getSchool(), u.getOneRosterId(),
                                u.getRole().toString());

                        String name = (String) user.get("name");
                        dto.setReviewUsername(u.getUsername() != null ? u.getUsername() : name);
                    });
        });

        userRepository.findById(report.getReporterUserId())
                .ifPresent(u -> {
                    Map<String, Object> user = lookupService.getUser(u.getSchool(), u.getOneRosterId(),
                            u.getRole().toString());
                    String name = (String) user.get("name");
                    dto.setReporterUsername(u.getUsername() != null ? u.getUsername() : name);
                });

        return dto;
    }
}
