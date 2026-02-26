package be.ap.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "school")
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Long id;

    @Column(nullable = true, length = 500, name = "name")
    private String name;

    @Column(nullable = true, length = 500, name = "adres")
    private String adres;

    @Column(nullable = true, length = 500, name = "contact")
    private String contact;

    @Column(nullable = true, length = 1000, name = "description")
    private String description;

    @Column(nullable = true, name = "smartschool_id")
    private Long smartschoolId;

    public School() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAdres() { return adres; }
    public void setAdres(String adres) { this.adres = adres; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getSmartschoolId() { return smartschoolId; }
    public void setSmartschoolId(Long smartschoolId) { this.smartschoolId = smartschoolId; }
}
