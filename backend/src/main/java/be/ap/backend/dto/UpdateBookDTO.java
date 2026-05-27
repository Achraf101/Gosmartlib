package be.ap.backend.dto;

import java.time.Year;
import java.util.Set;

import be.ap.backend.enums.Clib;
import be.ap.backend.enums.FontSize;
import lombok.Data;

@Data
public class UpdateBookDTO {
    private String title;
    private String isbn;
    private String description;
    private Boolean fiction;
    private Boolean didactic;
    private Integer pages;
    private Year published;
    private String cover;
    private FontSize fontSize;
    private Clib clib;
    private Long author;
    private Long publisher;
    private Long language;
    private Long bookType;
    private Long series;
    private Integer seriesNumber;
    private Set<Long> genres;
    private Set<Long> themes;
}
