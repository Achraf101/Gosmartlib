package be.ap.backend.entity;

import java.time.Year;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

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


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getIsbn() {
        return isbn;
    }


    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public long getBookAuthorId() {
        return bookAuthorId;
    }


    public void setBookAuthorId(long bookAuthorId) {
        this.bookAuthorId = bookAuthorId;
    }


    public long getPublisherId() {
        return publisherId;
    }


    public void setPublisherId(long publisherId) {
        this.publisherId = publisherId;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public Boolean getFiction() {
        return fiction;
    }


    public void setFiction(Boolean fiction) {
        this.fiction = fiction;
    }


    public Year getPublished() {
        return published;
    }


    public void setPublished(Year published) {
        this.published = published;
    }


    public String getCover() {
        return cover;
    }


    public void setCover(String cover) {
        this.cover = cover;
    }


    public long getLanguageId() {
        return languageId;
    }


    public void setLanguageId(long languageId) {
        this.languageId = languageId;
    }


    public byte getAgeStart() {
        return ageStart;
    }


    public void setAgeStart(byte ageStart) {
        this.ageStart = ageStart;
    }


    public byte getAgeEnd() {
        return ageEnd;
    }


    public void setAgeEnd(byte ageEnd) {
        this.ageEnd = ageEnd;
    }


    public int getPages() {
        return pages;
    }


    public void setPages(int pages) {
        this.pages = pages;
    }


    public int getRating() {
        return rating;
    }


    public void setRating(int rating) {
        this.rating = rating;
    }


    public int getRatingCount() {
        return ratingCount;
    }


    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }


    public byte getFontSize() {
        return fontSize;
    }


    public void setFontSize(byte fontSize) {
        this.fontSize = fontSize;
    }


    public long getSchoolId() {
        return schoolId;
    }


    public void setSchoolId(long schoolId) {
        this.schoolId = schoolId;
    }
}