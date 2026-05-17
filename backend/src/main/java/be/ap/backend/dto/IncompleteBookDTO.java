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
    private List<String> missingFields;
}