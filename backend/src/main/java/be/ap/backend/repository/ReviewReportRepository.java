package be.ap.backend.repository;

import be.ap.backend.entity.ReviewReport;
import be.ap.backend.enums.ReviewReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
    List<ReviewReport> findByStatus(ReviewReportStatus status);
    boolean existsByReviewIdAndReporterUserId(Long reviewId, Long reporterUserId);
}
