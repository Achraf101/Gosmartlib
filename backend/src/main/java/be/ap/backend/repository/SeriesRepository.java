package be.ap.backend.repository;

import be.ap.backend.entity.Series;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeriesRepository extends JpaRepository<Series, Long> {

    List<Series> findByNameContainingIgnoreCase(String name);

    List<Series> findByAuthorId(Long authorId);
    
}