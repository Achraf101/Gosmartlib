package be.ap.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import be.ap.backend.entity.Book;
import be.ap.backend.entity.LoanBook;

@Repository
public interface LoanBookRepository extends JpaRepository<LoanBook, Long> {
    List<LoanBook> findByLoanId(Long loanId);

    @Query("SELECT lb.book FROM LoanBook lb WHERE lb.loan.id = :loanId")
    List<Book> getBooksByLoanId(@Param("loanId") Long loanId);
}
