package be.ap.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "campus_book")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CampusBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id", nullable = false)
    private Campus campus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "current_amount", nullable = false)
    private Integer currentAmount;

    @Column(name = "location", length = 255)
    private String location;
}