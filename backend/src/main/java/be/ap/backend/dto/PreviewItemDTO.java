package be.ap.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO representing a single row in a book import preview, indicating whether
 * the ISBN was resolved.
 */
@Getter
@AllArgsConstructor
public class PreviewItemDTO {
    private final int row;
    private final String isbn;
    private final boolean found;
    private final String title;
    private final String author;
    private final String coverUrl;
}