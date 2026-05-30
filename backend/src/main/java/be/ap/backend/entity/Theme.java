package be.ap.backend.entity;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Represents a thematic classification used to group books by subject or
 * concept.
 *
 * <p>
 * A theme can be associated with multiple books and is uniquely identified by
 * its name.
 * </p>
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "books")
@ToString(exclude = "books")
@Entity
@Table(name = "theme", indexes = { @Index(name = "index_theme_name", columnList = "name", unique = true) })
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JsonIgnore
    @OneToMany(mappedBy = "themes")
    private Set<Book> books;

    @Column(length = 255, name = "name", nullable = false)
    private String name;

    public Theme(String name) {
        this.name = name;
    }
}
