package be.ap.backend.dto;

import be.ap.backend.entity.BookType;
import be.ap.backend.entity.Language;
import be.ap.backend.enums.Clib;

import java.time.Year;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class BookResultDTO {
    private Long id;
    private String title;
    private String cover;
    private String authorName;
    private BookType bookType;
    private Long seriesId;
    private String seriesName;
    private Integer seriesNumber;
    private Language language;
    private Year published;
    private String description;
    private Set<GenreDTO> genres = new HashSet<>();
    private Set<ThemeDTO> themes = new HashSet<>();
    private boolean fiction;
    private Clib clib;
    private int pages;
    private int rating;
    private int ratingCount;

    public BookResultDTO(Long id, String title, String cover, String authorName, BookType bookType,
            Long seriesId, String seriesName, Integer seriesNumber, Language language,
            Year published, String description, boolean fiction, Clib clib,
            int pages, int rating, int ratingCount) {
        this.id = id;
        this.title = title;
        this.cover = cover;
        this.authorName = authorName;
        this.bookType = bookType;
        this.seriesId = seriesId;
        this.seriesName = seriesName;
        this.seriesNumber = seriesNumber;
        this.language = language;
        this.published = published;
        this.description = description;
        this.fiction = fiction;
        this.clib = clib;
        this.pages = pages;
        this.rating = rating;
        this.ratingCount = ratingCount;
    }
}