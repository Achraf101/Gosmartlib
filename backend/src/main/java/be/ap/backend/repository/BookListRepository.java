package be.ap.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import be.ap.backend.entity.BookList;

import java.util.List;
import java.util.Optional;

public interface BookListRepository extends JpaRepository<BookList, Long> {
    Optional<BookList> findByShareToken(String shareToken);

    List<BookList> findByOwnerId(Long ownerId);

    /**
     * Returns all lists owned by the given user that do not yet contain the given
     * book.
     */
    @Query("""
                SELECT bl
                FROM BookList bl
                WHERE bl.ownerId = :userId
                AND bl.id NOT IN (
                    SELECT bli.bookListId
                    FROM BookListItem bli
                    WHERE bli.bookId = :bookId
                )
            """)
    List<BookList> findListsWithoutBook(Long userId, Long bookId);
}
