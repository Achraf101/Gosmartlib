package be.ap.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "campus")
@Data
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

    @ManyToOne
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    // Constructors
    public Campus() {
    }

    public Campus(Long id, String name, String adres, int borrowLimit, School school) {
        this.id = id;
        this.name = name;
        this.adres = adres;
        this.borrowLimit = borrowLimit;
        this.school = school;
    }
}
