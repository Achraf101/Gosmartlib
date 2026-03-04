package be.ap.backend.entity;

import java.time.Year;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "book",
    indexes = {
        @Index(name = "index_books_isbn", columnList = "isbn"),
        @Index(name = "index_books_title", columnList = "title")

    }
)
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Long id;

    @Column(nullable = true, length = 13, unique = true, name = "isbn")
    private String isbn;
    
    @Column(length = 255, name = "title")
    private String title;

    @Column(nullable = true,  name = "book_author_id")
    private long bookAuthorId;

    @Column(nullable = true, name = "publisher_id")
    private long publisherId;

    @Column(nullable = true, length = 1000, name = "description")
    private String description;
    
    @Column(name = "fiction")
    private Boolean fiction;
    
    @Column(nullable = true, name = "published")
    private Year published;
    
    @Column(nullable = true, length = 255, name = "cover")
    private String cover;
    
    @Column(name = "language_id")
    private long languageId;
    
    @Column(nullable = true, name = "age_start")
    private byte ageStart;
    
    @Column(nullable = true, name = "age_end")
    private byte ageEnd;
    
    @Column(nullable = true, name = "pages")
    private int pages;
    
    @Column(name = "rating")
    private int rating;
    
    @Column(name = "rating_count")
    private int ratingCount;
    
    @Column(nullable = true, name = "font_size")
    private byte fontSize;
    
    @Column(nullable = true, name = "school_id")
    private long schoolId;


    public Book() {
    }
}