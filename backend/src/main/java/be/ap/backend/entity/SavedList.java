package be.ap.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Represents a saved book list reference created by a user.
 *
 * <p>
 * Stores a link between a user and a book list, including the creation
 * timestamp.
 * </p>
 */
@Data
@Entity
@Table(name = "saved_lists")
public class SavedList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "book_list_id", nullable = false)
    private Long bookListId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
