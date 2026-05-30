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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents an author in the system, including their bibliographic metadata
 * and relationships to books and contributions.
 */
@Entity
@Table(name = "author", indexes = { @Index(name = "index_genre_name", columnList = "name", unique = true) })
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(exclude = { "books", "contributors" })
@ToString(exclude = { "books", "contributors" })
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JsonIgnore
    @OneToMany(mappedBy = "author")
    private Set<Book> books;

    @JsonIgnore
    @OneToMany(mappedBy = "author")
    private Set<BookContributor> contributors;

    @Column(length = 255, name = "name", nullable = false)
    private String name;

    @Column(length = 1000, name = "description")
    private String description;

    public void setName(String name) {
        this.name = name.trim();
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}