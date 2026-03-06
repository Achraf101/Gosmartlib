package be.ap.backend.entity;

import java.time.Year;
import java.util.Set;

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
    @Enumerated(EnumType.STRING)
    private FontSize fontSize;

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

    public BookType getBookType() {
        return bookType;
    }

    public void setBookType(BookType bookType) {
        this.bookType = bookType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<Genre> getGenres() {
        return genres;
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Long getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(Long seriesId) {
        this.seriesId = seriesId;
    }

    public int getSeriesCount() {
        return seriesCount;
    }

    public void setSeriesCount(int seriesCount) {
        this.seriesCount = seriesCount;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Set<BookContributor> getContributors() {
        return contributors;
    }

    public void setContributors(Set<BookContributor> contributors) {
        this.contributors = contributors;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
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

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
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

    public FontSize getFontSize() {
        return fontSize;
    }

    public void setFontSize(FontSize fontSize) {
        this.fontSize = fontSize;
    }

    public long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(long schoolId) {
        this.schoolId = schoolId;
    }

}