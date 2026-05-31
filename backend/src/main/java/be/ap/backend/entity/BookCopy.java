package be.ap.backend.entity;

import be.ap.backend.enums.CopyStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a physical copy of a book within a specific library location.
 *
 * <p>
 * Each copy has a unique accession identifier and a status indicating its
 * current condition.
 * </p>
 */
@Entity
@Table(name = "book_copy")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "accession_id", unique = true, nullable = false, length = 12)
    private String accessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_book_id", nullable = false)
    private LocationBook locationBook;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CopyStatus status = CopyStatus.AVAILABLE;

    @Column(columnDefinition = "TEXT")
    private String note;
}
