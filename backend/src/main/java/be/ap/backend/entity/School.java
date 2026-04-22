package be.ap.backend.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "school")
@Data
@NoArgsConstructor
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Long id;

    @Column(nullable = false, length = 255, name = "name")
    private String name;

    @Column(nullable = true, length = 500, name = "adres")
    private String adres;

    @Column(nullable = true, length = 500, name = "contact")
    private String contact;

    @Column(nullable = true, length = 1000, name = "description")
    private String description;

    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL)
    private List<Campus> campuses = new ArrayList<>();

    public School(String name, String adres, String contact, String description) {
        this.name = name;
        this.adres = adres;
        this.contact = contact;
        this.description = description;
    }
}