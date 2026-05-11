package be.ap.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import be.ap.backend.entity.LoanBook;

@Repository
public interface LoanBookRepository extends JpaRepository<LoanBook, Long> {
    List<LoanBook> findByLoanId(Long loanId);
}
