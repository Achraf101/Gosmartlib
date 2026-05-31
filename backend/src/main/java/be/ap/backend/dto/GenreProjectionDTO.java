package be.ap.backend.dto;

/**
 * Projection mapping a book to one of its genres.
 */
public interface GenreProjectionDTO {
    Long getBookId();

    Long getGenreId();

    String getGenreName();
}
