package be.ap.backend.entity;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@Entity
@EqualsAndHashCode(exclude = "books")
@ToString(exclude = "books")
@Table(name = "language")
public class Language {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JsonIgnore
    @OneToMany(mappedBy = "language")
    private Set<Book> books;

    @Column(length = 255, name = "name", unique = true, nullable = false)
    private String name;

    @Column(length = 2, name = "code", unique = true, nullable = false)
    private String code;

    public Language(String name, String code) {
        this.name = name;
        this.code = code;
    }
}