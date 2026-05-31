package be.ap.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO representing a book genre.
 */
@Getter
@AllArgsConstructor
public class GenreDTO {
    private Long id;
    private String name;

}
