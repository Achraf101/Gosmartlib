package be.ap.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a bookmarked book by a user.
 *
 * <p>
 * Stores the relationship between a user and a book, including the timestamp
 * when the bookmark was created.
 * </p>
 */
@Entity
@Table(name = "bookmarked", indexes = @Index(columnList = "user_id, book_id", unique = true))
@Getter
@Setter
@NoArgsConstructor
public class Bookmarked {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private LocalDateTime added;

    @PrePersist
    public void prePersist() {
        this.added = LocalDateTime.now();
    }
}