package be.ap.backend.dto;

import be.ap.backend.entity.BookType;
import be.ap.backend.entity.Language;
import be.ap.backend.entity.Clib;
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
    private String author_name;
    private BookType book_type;
    private Long series_id;
    private int series_count;
    private Language language;
    private Year published;
    private String description;
    private Set<GenreDTO> genres = new HashSet<>(); // Deze vullen we handmatig in de service
    private boolean fiction;
    private Clib clib;
    private int pages;
    private int rating;
    private int rating_count;

    // Handmatige constructor voor de Repository Query (zonder genres!)
    public BookResultDTO(Long id, String title, String cover, String author_name, BookType book_type, Long series_id,
            int series_count, Language language, Year published, String description, boolean fiction,
            Clib clib, int pages, int rating, int rating_count) {
        this.id = id;
        this.title = title;
        this.cover = cover;
        this.author_name = author_name;
        this.book_type = book_type;
        this.series_id = series_id;
        this.series_count = series_count;
        this.language = language;
        this.published = published;
        this.description = description;
        this.fiction = fiction;
        this.clib = clib;
        this.pages = pages;
        this.rating = rating;
        this.rating_count = rating_count;
    }
}