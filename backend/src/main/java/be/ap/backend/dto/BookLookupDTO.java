package be.ap.backend.dto;

import lombok.Data;
import java.util.List;

/**
 * Data transfer object representing book metadata retrieved from an external
 * lookup source.
 *
 * <p>
 * Used to map and expose book information such as title, authorship,
 * publication details, and identifiers obtained from external services.
 * </p>
 */
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
