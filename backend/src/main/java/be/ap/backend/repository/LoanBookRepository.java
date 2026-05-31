package be.ap.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import be.ap.backend.entity.LoanBook;
import be.ap.backend.mapper.BookWithAmount;

@Repository
public interface LoanBookRepository extends JpaRepository<LoanBook, Long> {
    List<LoanBook> findByLoanId(Long loanId);

    /**
     * Returns the books and their requested amounts for the given loan.
     */
    @Query("SELECT new be.ap.backend.mapper.BookWithAmount(lb.book, lb.requestedAmount) FROM LoanBook lb WHERE lb.loan.id = :loanId")
    List<BookWithAmount> getBooksByLoanId(@Param("loanId") Long loanId);
}
