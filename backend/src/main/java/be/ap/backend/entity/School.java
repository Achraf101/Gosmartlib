package be.ap.backend.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "school")
@Data
public class School {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "adres", length = 500)
    private String adres;

    @Column(name = "contact", length = 500)
    private String contact;

    @Column(name = "description", length = 1000)
    private String description;

    // One school has many campuses
    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Campus> campuses;

    public School() {

    }

    public School(String name, String adres, String contact, String description) {
        this.name = name;
        this.adres = adres;
        this.contact = contact;
        this.description = description;
    }
}
