package be.ap.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "section_book")
public class SectionBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(name = "ranking")
    private short ranking;
}