package be.ap.backend.dto;

import java.time.Year;
import java.util.List;

import be.ap.backend.enums.Clib;
import be.ap.backend.enums.FontSize;
import lombok.Data;

/**
 * Data transfer object used to create a new book entry.
 *
 * <p>
 * Contains all input fields required for book creation, including metadata,
 * relations (author, publisher, genres, themes), and optional enrichment
 * fields such as cover and contributors.
 * </p>
 */
@Data
public class CreateBookDTO {
    private Long bookType;
    private String title;
    private Long author;
    private String cover;
    private String coverUrl;
    private String isbn;
    private Long series;
    private Integer seriesCount;
    private List<Long> contributors;
    private Long publisher;
    private List<Long> genres;
    private List<Long> themes;
    private String description;
    private Boolean fiction;
    private Year published;
    private Long language;
    private Clib clib;
    private int pages;
    private FontSize fontSize;
    private long schoolId;
    private Boolean didactic;
}