package be.ap.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import be.ap.backend.entity.Material;
import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    // @Query("SELECT m FROM material m WHERE m.book.id = :bookId ORDER BY
    // m.uploaded DESC")
    List<Material> findByBook_IdOrderByUploadedDesc(@Param("bookId") Long bookId);
}
