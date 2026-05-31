package be.ap.backend.controller;

import be.ap.backend.dto.BookmarkedDTO;
import be.ap.backend.service.BookmarkedService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing bookmarked books per user.
 *
 * <p>
 * Provides endpoints to retrieve, check, and toggle bookmarks.
 * </p>
 */
@RestController
@RequestMapping("bookmarked")
@RequiredArgsConstructor
public class BookmarkedController {

    private final BookmarkedService bookmarkedService;

    /**
     * Retrieves all bookmarked books for a specific user.
     *
     * @param userId user identifier
     * @return list of bookmarked books
     */
    @GetMapping("/{userId}")
    public List<BookmarkedDTO> getBookmarkeds(@PathVariable Long userId) {
        return bookmarkedService.getBookmarked(userId);
    }

    /**
     * Checks whether a book is bookmarked by a user.
     *
     * @param userId user identifier
     * @param bookId book identifier
     * @return true if bookmarked, false otherwise
     */
    @GetMapping("/{userId}/{bookId}")
    public boolean isBookmarked(@PathVariable Long userId, @PathVariable Long bookId) {
        return bookmarkedService.isBookmarked(userId, bookId);
    }

    /**
     * Toggles bookmark state for a book for a specific user.
     *
     * <p>
     * If the book is already bookmarked, it will be removed;
     * otherwise it will be added.
     * </p>
     *
     * @param userId user identifier
     * @param bookId book identifier
     * @return resulting bookmark state after toggle
     */
    @PostMapping("/{userId}/{bookId}")
    public boolean toggleBookmarked(@PathVariable Long userId, @PathVariable Long bookId) {
        return bookmarkedService.toggleBookmarked(userId, bookId);
    }
}