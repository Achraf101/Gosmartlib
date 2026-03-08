package be.ap.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "rating")
    private byte rating;

    @Column(length = 500, name = "content")
    private String content;

    @Column(name = "campus_id")
    private Long campusId;

    @Column(name = "hidden")
    private boolean hidden;

    @Column(name = "added")
    private LocalDateTime added;
}