package be.ap.backend.repository;

import be.ap.backend.entity.BookCopy;
import be.ap.backend.enums.CopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    Optional<BookCopy> findByAccessionId(String accessionId);

    List<BookCopy> findByLocationBookId(Long locationBookId);

    List<BookCopy> findByLocationBookIdAndStatus(Long locationBookId, CopyStatus status);

    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(accession_id, 5) AS UNSIGNED)), 0) FROM book_copy", nativeQuery = true)
    int findMaxSequenceNumber();
}
