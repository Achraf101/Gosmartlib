package be.ap.backend.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Represents a school entity containing configuration, contact information,
 * and all associated academic and library-related data.
 *
 * <p>
 * This entity acts as a root aggregate for locations, classrooms, users,
 * enrollments, and materials.
 * </p>
 */
@Entity
@Table(name = "school")
@Data
@NoArgsConstructor
public class School implements Serializable {

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

    @Column(name = "borrow_limit", nullable = false)
    private int borrowLimit;

    @Column(name = "borrow_period", nullable = false)
    private int borrowPeriod;

    @Column(name = "extend_period", nullable = false)
    private int extendPeriod;

    @Column(name = "extend_limit", nullable = false)
    private int extendLimit;

    @Column(nullable = false, unique = true, length = 255, name = "ss_subdomain")
    private String ssSubdomain;

    @Column(name = "oneroster_client_id", length = 500)
    private String oneRosterClientId;

    @Column(name = "oneroster_client_secret", length = 500)
    private String oneRosterClientSecret;

    @Column(name = "ss_id", length = 255)
    private String ssId;

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL)
    private List<Location> locations = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL)
    private List<Classroom> classrooms = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL)
    private List<User> users = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL)
    private List<Material> materials = new ArrayList<>();

    public School(String name, String adres, String contact, String description, int borrowLimit, int borrowPeriod,
            int extendPeriod, int extendLimit, String ssSubdomain, String oneRosterClientId,
            String oneRosterClientSecret) {
        this.name = name;
        this.adres = adres;
        this.contact = contact;
        this.description = description;
        this.borrowLimit = borrowLimit;
        this.borrowPeriod = borrowPeriod;
        this.extendPeriod = extendPeriod;
        this.extendLimit = extendLimit;
        this.ssSubdomain = ssSubdomain;
        this.oneRosterClientId = oneRosterClientId;
        this.oneRosterClientSecret = oneRosterClientSecret;
    }
}