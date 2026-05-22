package be.ap.backend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IncompleteBookDTO {
    private int row;
    private String isbn;
    private String title;
    private String authorName;
    private String description;
    private String publisherName;
    private Integer publishedYear;
    private Integer pages;
    private String coverUrl;
    private String languageCode;
    private String languageName;
    private String bookTypeName;
    private String genresRaw;
    private String themesRaw;
    private String fontSize;
    private String fiction;
    private String didactic;
    private String schoolOnly;
    private String clib;
    private List<String> missingFields;
    private List<String> invalidFields;
}