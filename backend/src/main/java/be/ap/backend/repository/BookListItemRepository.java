package be.ap.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import be.ap.backend.entity.BookListItem;
import jakarta.transaction.Transactional;

public interface BookListItemRepository extends JpaRepository<BookListItem, Long> {
    List<BookListItem> findByBookListId(Long listId);

    /**
     * Removes the entry for the given book from the given list.
     */
    @Transactional
    void deleteByBookIdAndBookListId(Long bookId, Long bookListId);
}
