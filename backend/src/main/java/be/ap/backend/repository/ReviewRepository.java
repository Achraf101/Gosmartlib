package be.ap.backend.repository;

import be.ap.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByBookIdAndHiddenFalse(Long bookId);

    /**
     * Returns the average rating for the given book, excluding hidden reviews.
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId AND r.hidden = false")
    Double findAverageRatingByBookId(@Param("bookId") Long bookId);

    /**
     * Returns the number of visible reviews for the given book.
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.book.id = :bookId AND r.hidden = false")
    Long findReviewCountByBookId(@Param("bookId") Long bookId);

    /**
     * Returns all visible reviews by the given user, ordered by date descending.
     */
    @Query("SELECT r FROM Review r WHERE r.userId = :userId AND r.hidden = false ORDER BY r.added DESC")
    List<Review> findByUserIdAndHiddenFalse(@Param("userId") Long userId);

    /**
     * Returns the average rating given by the given user, excluding hidden reviews.
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.userId = :userId AND r.hidden = false")
    Double findAverageRatingByUserId(@Param("userId") Long userId);
}