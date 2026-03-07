package be.ap.backend.dto;

import java.time.Year;
import java.util.List;

import be.ap.backend.entity.FontSize;

public class CreateBookDTO {

    private Long bookType;

    private String title;

    private Long author;

    private String cover;

    private String isbn;

    private Long series;

    private int seriesCount;

    private List<Long> contributors;

    private Long publisher;

    private List<Long> genres;

    private String description;

    private Boolean fiction;

    private Year published;

    private Long language;

    private byte ageStart;

    private byte ageEnd;

    private int pages;

    private FontSize fontSize;

    private long schoolId;

    public Long getBookType() {
        return bookType;
    }

    public void setBookType(Long bookType) {
        this.bookType = bookType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getAuthor() {
        return author;
    }

    public void setAuthor(Long author) {
        this.author = author;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Long getSeries() {
        return series;
    }

    public void setSeries(Long series) {
        this.series = series;
    }

    public int getSeriesCount() {
        return seriesCount;
    }

    public void setSeriesCount(int seriesCount) {
        this.seriesCount = seriesCount;
    }

    public List<Long> getContributors() {
        return contributors;
    }

    public void setContributors(List<Long> contributors) {
        this.contributors = contributors;
    }

    public Long getPublisher() {
        return publisher;
    }

    public void setPublisher(Long publisher) {
        this.publisher = publisher;
    }

    public List<Long> getGenres() {
        return genres;
    }

    public void setGenres(List<Long> genres) {
        this.genres = genres;
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

    public Long getLanguage() {
        return language;
    }

    public void setLanguage(Long language) {
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
