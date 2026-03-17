package be.ap.backend.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import be.ap.backend.entity.CampusBook;

@Repository
public interface CampusBookRepository extends JpaRepository<CampusBook, Long> {
    boolean existsByCampusIdAndBookId(Long campusId, Long bookId);

    Page<CampusBook> findByCampusId(Long campusId, Pageable pageable);
}
