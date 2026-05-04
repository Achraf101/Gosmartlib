package be.ap.backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import be.ap.backend.entity.Loan;
import be.ap.backend.entity.LoanStatus;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    @Query("SELECT DISTINCT l FROM Loan l LEFT JOIN FETCH l.loanBooks lb LEFT JOIN FETCH lb.book WHERE l.status = :status ORDER BY l.created ASC")
    List<Loan> findByStatusWithBooks(@Param("status") LoanStatus status);

    List<Loan> findByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT l FROM Loan l LEFT JOIN FETCH l.loanBooks lb LEFT JOIN FETCH lb.book WHERE l.closed = false AND l.status IN :statuses AND l.end < :today AND l.campus.id = :campusId ORDER BY l.end ASC")
    List<Loan> findOverdueLoans(@Param("statuses") List<LoanStatus> statuses, @Param("today") LocalDate today, @Param("campusId") Long campusId);

    @Query("SELECT DISTINCT l FROM Loan l LEFT JOIN FETCH l.loanBooks lb LEFT JOIN FETCH lb.book WHERE l.start >= :from AND l.start <= :to AND l.status IN :statuses AND l.campus.id = :campusId ORDER BY l.created DESC")
    List<Loan> findByDateRangeWithBooks(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("statuses") List<LoanStatus> statuses, @Param("campusId") Long campusId);

    @Query("SELECT COUNT(DISTINCT l) FROM Loan l " +
    "WHERE l.closed = false " +
    "AND l.status IN :statuses " +
    "AND l.end < :today " +
    "AND l.campus.id = :campusId")
    int countOverdueLoans(@Param("statuses") List<LoanStatus> statuses, @Param("today") LocalDate today, @Param("campusId") Long campusId);

    @Query("SELECT DISTINCT l FROM Loan l LEFT JOIN FETCH l.loanBooks lb LEFT JOIN FETCH lb.book WHERE l.closed = false AND l.status IN :statuses AND l.end >= :today AND l.end <= :inSevenDays AND l.campus.id = :campusId ORDER BY l.end ASC")
    List<Loan> findDueSoonLoans(@Param("statuses") List<LoanStatus> statuses, @Param("today") LocalDate today, @Param("inSevenDays") LocalDate inSevenDays, @Param("campusId") Long campusId);

    @Query("SELECT COUNT(DISTINCT l) FROM Loan l " +
    "WHERE l.closed = false " +
    "AND l.status IN :statuses " +
    "AND l.end >= :today " +
    "AND l.end <= :inSevenDays " +
    "AND l.campus.id = :campusId")
    int countDueSoonLoans(@Param("statuses") List<LoanStatus> statuses, @Param("today") LocalDate today, @Param("inSevenDays") LocalDate inSevenDays, @Param("campusId") Long campusId);

    @Query("SELECT g.name, COUNT(DISTINCT l) FROM Loan l " +
    "JOIN l.loanBooks lb " +
    "JOIN lb.book b " +
    "JOIN b.genres g " +
    "WHERE l.status IN :statuses " +
    "AND l.start >= :from AND l.start <= :to " +
    "AND l.campus.id = :campusId " +
    "GROUP BY g.name " +
    "ORDER BY COUNT(DISTINCT l) DESC")
    List<Object[]> findTopGenres(@Param("statuses") List<LoanStatus> statuses, @Param("from") LocalDate from, @Param("to") LocalDate to, @Param("campusId") Long campusId);
}