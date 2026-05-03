package be.ap.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

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

    @Column(name = "campus_id")
    private Long campusId;

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