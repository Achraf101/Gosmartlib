package be.ap.backend.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import be.ap.backend.entity.LoanBook;

@Repository
public interface LoanBookRepository extends JpaRepository<LoanBook, Long> {

}
