package be.ap.backend.entity;

import be.ap.backend.enums.ContributorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the relationship between a book and an author with a specific
 * contribution type.
 *
 * <p>
 * This entity models many-to-many enrichment between books and authors,
 * allowing classification of the author's role (e.g. co-author, illustrator).
 * </p>
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "book_contributor")
public class BookContributor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private Author author;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ContributorType type;
}