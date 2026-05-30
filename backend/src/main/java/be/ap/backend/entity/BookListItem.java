package be.ap.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Represents an item inside a book list, linking a book list to a specific
 * book.
 *
 * <p>
 * Ensures that each book can only appear once per book list via a unique
 * constraint.
 * </p>
 */
@Data
@Entity
@Table(name = "book_list_item", indexes = @Index(columnList = "book_list_id, book_id", unique = true))
public class BookListItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_list_id", nullable = false)
    private Long bookListId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;
}