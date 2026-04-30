package be.ap.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class BookLookupDTO {
    private String title;
    private String authorName;
    private String publisherName;
    private String description;
    private Integer publishedYear;
    private Integer pages;
    private String languageCode;
    private List<String> contributors;
    private String isbn;
    private String coverUrl;
}
