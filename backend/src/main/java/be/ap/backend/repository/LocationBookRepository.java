package be.ap.backend.repository;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import be.ap.backend.entity.LocationBook;

@Repository
public interface LocationBookRepository extends JpaRepository<LocationBook, Long> {
    boolean existsByLocationIdAndBookId(Long locationId, Long bookId);

    Page<LocationBook> findByLocationId(Long locationId, Pageable pageable);

    Optional<LocationBook> findByLocationIdAndBookId(Long locationId, Long bookId);

    List<LocationBook> findByBookIdAndLocationSchoolId(Long bookId, Long schoolId);

    /**
     * Returns the total stock across all locations for the given school.
     */
    @Query("SELECT SUM(cb.amount) FROM LocationBook cb WHERE cb.location.school.id = :schoolId")
    Integer sumAmountBySchoolId(@Param("schoolId") Long schoolId);

    /**
     * Returns the total available stock across all locations for the given school.
     */
    @Query("SELECT SUM(cb.currentAmount) FROM LocationBook cb WHERE cb.location.school.id = :schoolId")
    Integer sumCurrentAmountBySchoolId(@Param("schoolId") Long schoolId);
}
