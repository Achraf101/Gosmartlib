package be.ap.backend.dto;

import java.time.LocalDateTime;

import be.ap.backend.entity.Author;

/**
 * Data transfer object representing a bookmarked book entry for a user.
 *
 * <p>
 * Used to expose minimal information about a book that has been bookmarked,
 * including identity, display metadata, and the timestamp when it was added.
 * </p>
 */
public record BookmarkedDTO(Long id,
        Long bookId,
        String title,
        String cover,
        Author author,
        LocalDateTime added) {
}
