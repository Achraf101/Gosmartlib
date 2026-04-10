package be.ap.backend.dto;

import java.time.Year;
import java.util.List;

import be.ap.backend.entity.FontSize;
import lombok.Data;

@Data
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

}
