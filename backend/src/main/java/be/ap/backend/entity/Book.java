package be.ap.backend.entity;

import java.time.Year;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = { "genres", "contributors" })
@ToString(exclude = { "genres", "contributors" })
@Entity
@Table(name = "book", indexes = {
        @Index(name = "index_books_isbn", columnList = "isbn", unique = true),
        @Index(name = "index_books_title", columnList = "title")
})
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "book_type_id")
    private BookType bookType;

    @Column(length = 255, name = "title")
    private String title;

    @ManyToMany
    @JoinTable(name = "book_genre", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genres;

    @Column(nullable = true, length = 13, name = "isbn")
    private String isbn;

    @Column(name = "series_id")
    private Long seriesId;

    @Column(name = "series_count")
    private int seriesCount;

    @ManyToOne
    @JoinColumn(nullable = true, name = "author_id")
    private Author author;

    @JsonIgnore
    @OneToMany(mappedBy = "book")
    private Set<BookContributor> contributors;

    @ManyToOne
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @Column(nullable = true, length = 1000, name = "description")
    private String description;

    @Column(name = "fiction")
    private Boolean fiction;

    @Column(nullable = true, name = "published")
    private Year published;

    @Column(nullable = true, length = 255, name = "cover")
    private String cover;

    @ManyToOne
    private Language language;

    @Column(nullable = true, name = "clib")
    @Enumerated(EnumType.STRING)
    private Clib clib;

    @Column(nullable = true, name = "pages")
    private Integer pages;

    @Column(name = "rating")
    private int rating;

    @Column(name = "rating_count")
    private int ratingCount;

    @Column(nullable = true, name = "font_size")
    @Enumerated(EnumType.STRING)
    private FontSize fontSize;

    @Column(nullable = true, name = "school_id")
    private long schoolId;
}