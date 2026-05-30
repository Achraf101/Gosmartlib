package be.ap.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Represents a user review for a book, including rating, optional text content,
 * visibility state, and metadata such as creation timestamp.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "rating", nullable = false)
    private double rating;

    @Column(name = "content", length = 500)
    private String content;

    @Column(name = "hidden")
    private boolean hidden = false;

    @Column(name = "added")
    private LocalDateTime added;

    @Column(name = "user_id")
    private Long userId;

    @PrePersist
    public void prePersist() {
        this.added = LocalDateTime.now();
    }
}