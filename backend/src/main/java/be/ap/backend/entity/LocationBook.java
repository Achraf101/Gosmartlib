package be.ap.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "location_book")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LocationBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "current_amount", nullable = false)
    private Integer currentAmount;

    @Column(name = "note", length = 255)
    private String note;
}