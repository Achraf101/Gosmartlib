package be.ap.backend.repository;

import org.springframework.data.domain.Pageable;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import be.ap.backend.entity.CampusBook;

@Repository
public interface CampusBookRepository extends JpaRepository<CampusBook, Long> {
    boolean existsByCampusIdAndBookId(Long campusId, Long bookId);

    Page<CampusBook> findByCampusId(Long campusId, Pageable pageable);

    Optional<CampusBook> findByCampusIdAndBookId(Long campusId, Long bookId);

    @Query("SELECT SUM(cb.amount) FROM CampusBook cb WHERE cb.campus.id = :campusId")
    Integer sumAmountByCampusId(@Param("campusId") Long campusId);

    @Query("SELECT SUM(cb.currentAmount) FROM CampusBook cb WHERE cb.campus.id = :campusId")
    Integer sumCurrentAmountByCampusId(@Param("campusId") Long campusId);
}
