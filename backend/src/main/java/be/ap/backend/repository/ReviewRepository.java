package be.ap.backend.repository;

import be.ap.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.added >= :since AND r.hidden = false")
    List<Review> findRecentReviews(@Param("since") LocalDateTime since);
}