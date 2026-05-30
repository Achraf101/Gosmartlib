package be.ap.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a book entry within a loan, tracking requested, received, and
 * returned quantities,
 * as well as associated physical book copies.
 */
@Entity
@Table(name = "loan_book")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LoanBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne
    @JoinColumn(name = "book_copy_id")
    private BookCopy bookCopy;

    @Column(name = "requested_amount", nullable = false)
    private Integer requestedAmount;

    @Column(name = "received_amount", nullable = false)
    private Integer receivedAmount;

    @Column(name = "returned_amount", nullable = false)
    private Integer returnedAmount;

    @ElementCollection
    @CollectionTable(name = "loan_book_scanned_copies", joinColumns = @JoinColumn(name = "loan_book_id"))
    @Column(name = "book_copy_id")
    private Set<Long> scannedCopyIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "loan_book_returned_copies", joinColumns = @JoinColumn(name = "loan_book_id"))
    @Column(name = "book_copy_id")
    private Set<Long> returnedCopyIds = new HashSet<>();

}
