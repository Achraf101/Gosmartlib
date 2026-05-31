package be.ap.backend.dto;

import be.ap.backend.entity.Author;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

/**
 * Data transfer object representing a lightweight view of a book.
 *
 * <p>
 * Used for card-style UI representations where only minimal book
 * information is required (e.g. listings, recommendations).
 * </p>
 */
@Getter
@Setter
@AllArgsConstructor
public class BookCardDTO {
    private Long id;
    private String title;
    private String cover;
    private Author author;

}
