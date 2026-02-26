package be.ap.backend.model;

import java.time.Year;
import java.util.List;

public class BookDTO {

    private Long id;
    private String isbn;
    private String title;
    private String description;
    private Boolean fiction;
    private Year published;
    private String cover;
    private byte ageStart;
    private byte ageEnd;
    private int pages;
    private int rating;
    private int ratingCount;
    private String publisherName;
    private String languageName;
    private String languageCode;
    private List<String> authors;
    private List<String> genres;

    public BookDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getFiction() { return fiction; }
    public void setFiction(Boolean fiction) { this.fiction = fiction; }

    public Year getPublished() { return published; }
    public void setPublished(Year published) { this.published = published; }

    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }

    public byte getAgeStart() { return ageStart; }
    public void setAgeStart(byte ageStart) { this.ageStart = ageStart; }

    public byte getAgeEnd() { return ageEnd; }
    public void setAgeEnd(byte ageEnd) { this.ageEnd = ageEnd; }

    public int getPages() { return pages; }
    public void setPages(int pages) { this.pages = pages; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }

    public String getLanguageName() { return languageName; }
    public void setLanguageName(String languageName) { this.languageName = languageName; }

    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }

    public List<String> getAuthors() { return authors; }
    public void setAuthors(List<String> authors) { this.authors = authors; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }
}
