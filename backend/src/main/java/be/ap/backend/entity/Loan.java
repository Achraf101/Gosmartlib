package be.ap.backend.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loan")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // @ManyToOne
    // @JoinColumn(name = "campus_id", nullable = false)
    // private Campus campus;

    @Column(name = "extended", nullable = false)
    private Byte extended = 0;

    @Column(name = "start", nullable = false)
    private LocalDate start;

    @Column(name = "end", nullable = false)
    private LocalDate end;

    @Column(name = "note", length = 255)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LoanStatus status = LoanStatus.REQUESTED;

    @Column(name = "created", nullable = false, updatable = false)
    private LocalDateTime created = LocalDateTime.now();

    @Column(name = "closed", nullable = false)
    private Boolean closed = false;

    @OneToMany(mappedBy = "loan")
    private Set<LoanBook> loanBooks = new HashSet<>();
}
