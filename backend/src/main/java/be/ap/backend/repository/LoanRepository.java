package be.ap.backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import be.ap.backend.entity.Loan;
import jakarta.transaction.Transactional;
import be.ap.backend.enums.LoanStatus;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    /**
     * Returns all loans with the given status, with books eagerly fetched, ordered
     * by creation date.
     */
    @Query("SELECT DISTINCT l FROM Loan l LEFT JOIN FETCH l.loanBooks lb LEFT JOIN FETCH lb.book WHERE l.status = :status ORDER BY l.created ASC")
    List<Loan> findByStatusWithBooks(@Param("status") LoanStatus status);

    List<Loan> findByUserId(@Param("userId") Long userId);

    /**
     * Returns all open, overdue loans for the given school, with books eagerly
     * fetched, ordered by due date.
     */
    @Query("SELECT DISTINCT l FROM Loan l LEFT JOIN FETCH l.loanBooks lb LEFT JOIN FETCH lb.book WHERE l.closed = false AND l.status IN :statuses AND l.end < :today AND l.location.school.id = :schoolId ORDER BY l.end ASC")
    List<Loan> findOverdueLoans(@Param("statuses") List<LoanStatus> statuses, @Param("today") LocalDate today,
            @Param("schoolId") Long schoolId);

    /**
     * Returns all loans started within the given date range for the given school,
     * with books eagerly fetched.
     */
    @Query("SELECT DISTINCT l FROM Loan l LEFT JOIN FETCH l.loanBooks lb LEFT JOIN FETCH lb.book WHERE l.start >= :from AND l.start <= :to AND l.status IN :statuses AND l.location.school.id = :schoolId ORDER BY l.created DESC")
    List<Loan> findByDateRangeWithBooks(@Param("from") LocalDate from, @Param("to") LocalDate to,
            @Param("statuses") List<LoanStatus> statuses, @Param("schoolId") Long schoolId);

    /**
     * Returns the number of open, overdue loans for the given school.
     */
    @Query("SELECT COUNT(DISTINCT l) FROM Loan l " +
            "WHERE l.closed = false " +
            "AND l.status IN :statuses " +
            "AND l.end < :today " +
            "AND l.location.school.id = :schoolId")
    int countOverdueLoans(@Param("statuses") List<LoanStatus> statuses, @Param("today") LocalDate today,
            @Param("schoolId") Long schoolId);

    /**
     * Returns all open loans due within the next seven days for the given school,
     * with books eagerly fetched.
     */
    @Query("SELECT DISTINCT l FROM Loan l LEFT JOIN FETCH l.loanBooks lb LEFT JOIN FETCH lb.book WHERE l.closed = false AND l.status IN :statuses AND l.end >= :today AND l.end <= :inSevenDays AND l.location.school.id = :schoolId ORDER BY l.end ASC")
    List<Loan> findDueSoonLoans(@Param("statuses") List<LoanStatus> statuses, @Param("today") LocalDate today,
            @Param("inSevenDays") LocalDate inSevenDays, @Param("schoolId") Long schoolId);

    /**
     * Returns the number of open loans due within the next seven days for the given
     * school.
     */
    @Query("SELECT COUNT(DISTINCT l) FROM Loan l " +
            "WHERE l.closed = false " +
            "AND l.status IN :statuses " +
            "AND l.end >= :today " +
            "AND l.end <= :inSevenDays " +
            "AND l.location.school.id = :schoolId")
    int countDueSoonLoans(@Param("statuses") List<LoanStatus> statuses, @Param("today") LocalDate today,
            @Param("inSevenDays") LocalDate inSevenDays, @Param("schoolId") Long schoolId);

    /**
     * Returns genre names and their loan counts for the given school and date
     * range, ordered by count descending.
     */
    @Query("SELECT g.name, COUNT(DISTINCT l) FROM Loan l " +
            "JOIN l.loanBooks lb " +
            "JOIN lb.book b " +
            "JOIN b.genres g " +
            "WHERE l.status IN :statuses " +
            "AND l.start >= :from AND l.start <= :to " +
            "AND l.location.school.id = :schoolId " +
            "GROUP BY g.name " +
            "ORDER BY COUNT(DISTINCT l) DESC")
    List<Object[]> findTopGenres(@Param("statuses") List<LoanStatus> statuses, @Param("from") LocalDate from,
            @Param("to") LocalDate to, @Param("schoolId") Long schoolId);

    /**
     * Returns the number of loans for the given user within the given date range.
     */
    @Query("SELECT COUNT(DISTINCT l) FROM Loan l " +
            "WHERE l.user.id = :userId " +
            "AND l.status IN :statuses " +
            "AND l.start >= :from AND l.start <= :to")
    int countByUserIdAndStartBetween(@Param("userId") Long userId, @Param("statuses") List<LoanStatus> statuses,
            @Param("from") LocalDate from, @Param("to") LocalDate to);

    /**
     * Returns all loans for the given user matching the given statuses, with books
     * eagerly fetched.
     */
    @Query("SELECT DISTINCT l FROM Loan l LEFT JOIN FETCH l.loanBooks lb LEFT JOIN FETCH lb.book WHERE l.user.id = :userId AND l.status IN :statuses ORDER BY l.start DESC")
    List<Loan> findByUserIdWithBooks(@Param("userId") Long userId, @Param("statuses") List<LoanStatus> statuses);

    /**
     * Returns all returned loans for the given user, with books eagerly fetched,
     * ordered by due date.
     */
    @Query("SELECT DISTINCT l FROM Loan l LEFT JOIN FETCH l.loanBooks lb LEFT JOIN FETCH lb.book WHERE l.user.id = :userId AND l.status = :status ORDER BY l.end ASC")
    List<Loan> findReturnedByUserId(@Param("userId") Long userId, @Param("status") LoanStatus status);

    /**
     * Returns genre names and their loan counts for the given user and date range,
     * ordered by count descending.
     */
    @Query("SELECT g.name, COUNT(DISTINCT l) FROM Loan l " +
            "JOIN l.loanBooks lb " +
            "JOIN lb.book b " +
            "JOIN b.genres g " +
            "WHERE l.user.id = :userId " +
            "AND l.status IN :statuses " +
            "AND l.start >= :from AND l.start <= :to " +
            "GROUP BY g.name " +
            "ORDER BY COUNT(DISTINCT l) DESC")
    List<Object[]> findTopGenresByUserId(@Param("userId") Long userId, @Param("statuses") List<LoanStatus> statuses,
            @Param("from") LocalDate from, @Param("to") LocalDate to);

    /**
     * Returns all loans with the given status for the given school, with books
     * eagerly fetched, ordered by creation date.
     */
    @Query("SELECT DISTINCT l FROM Loan l LEFT JOIN FETCH l.loanBooks lb LEFT JOIN FETCH lb.book WHERE l.status = :state AND l.location.school.id = :schoolId ORDER BY l.created ASC")
    List<Loan> findByStateAndSchool(@Param("state") LoanStatus state, @Param("schoolId") Long schoolId);

    /**
     * Returns IDs of loans due tomorrow that have not yet been notified. Used by
     * the daily reminder scheduler.
     */
    @Query("SELECT l.id FROM Loan l WHERE l.notified = false AND l.end = :tomorrow")
    List<Long> getDueLoans(@Param("tomorrow") LocalDate tomorrow);

    /**
     * Marks the given loan as notified.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Loan l SET l.notified = true WHERE l.id = :id")
    void setNotifiedTrue(@Param("id") Long id);
}