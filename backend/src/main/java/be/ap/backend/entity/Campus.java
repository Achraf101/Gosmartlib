package be.ap.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "campus")
@Data
@NoArgsConstructor
public class Campus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "adres", length = 500)
    private String adres;

    @Column(name = "borrow_limit", nullable = false)
    private int borrowLimit;

    @Column(name = "borrow_period", nullable = false)
    private int borrowPeriod;

    @Column(name = "extend_period", nullable = false)
    private int extendPeriod;

    @Column(name = "extend_limit", nullable = false)
    private int extendLimit;

    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school;

    public Campus(String name, String adres, int borrowLimit, int borrowPeriod, int extendPeriod, int extendLimit) {
        this.name = name;
        this.adres = adres;
        this.borrowLimit = borrowLimit;
        this.borrowPeriod = borrowPeriod;
        this.extendPeriod = extendPeriod;
        this.extendLimit = extendLimit;
    }
}