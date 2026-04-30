package be.ap.backend.repository;

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
}