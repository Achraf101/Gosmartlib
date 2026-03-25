package be.ap.backend.repository;

import be.ap.backend.entity.Bookmarked;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkedRepository extends JpaRepository<Bookmarked, Long>  {

    List<Bookmarked> findByUserIdOrderByAdded(Long userId);

    Optional<Bookmarked> findByUserIdAndBookId(Long userId, Long bookId);

    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    void deleteByUserIdAndBookId(Long userId, Long bookId);
}
