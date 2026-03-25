package be.ap.backend.controller;

import be.ap.backend.dto.BookmarkedDTO;
import be.ap.backend.service.BookmarkedService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("bookmarked")
public class BookmarkedController {

    private final BookmarkedService bookmarkedService;

    public BookmarkedController(BookmarkedService bookmarkedService) {
        this.bookmarkedService = bookmarkedService;
    }

    @GetMapping("/{userId}")
    public List<BookmarkedDTO> getBookmarkeds(@PathVariable Long userId) {
        return bookmarkedService.getBookmarked(userId);
    }

    @GetMapping("/{userId}/{bookId}")
    public boolean isBookmarkedd(@PathVariable Long userId, @PathVariable Long bookId) {
        return bookmarkedService.isBookmarkedd(userId, bookId);
    }

    @PostMapping("/{userId}/{bookId}")
    public boolean toggleBookmarked(@PathVariable Long userId, @PathVariable Long bookId) {
        return bookmarkedService.toggleBookmarked(userId, bookId);
    }
}