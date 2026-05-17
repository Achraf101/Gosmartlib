package be.ap.backend.entity;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "location")
@Data
@NoArgsConstructor
public class Location implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "adres", length = 500)
    private String adres;

    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school;

    public Location(String name, String adres) {
        this.name = name;
        this.adres = adres;
    }
}