package be.ap.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.entity.BookType;
import be.ap.backend.service.BookTypeService;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing book types.
 *
 * <p>
 * Provides endpoints to retrieve, create, and search book type entities.
 * </p>
 */
@RestController
@RequestMapping("booktype")
@RequiredArgsConstructor
public class BookTypeController {

    private final BookTypeService bookTypeService;

    /**
     * Retrieves all available book types.
     *
     * @return list of book types
     */
    @GetMapping
    public ResponseEntity<List<BookType>> getAll() {
        return ResponseEntity.ok(bookTypeService.getAll());
    }

    /**
     * Creates a new book type.
     *
     * @param bookType book type entity to persist
     * @return created book type
     */
    @PostMapping
    public ResponseEntity<BookType> addBookType(@RequestBody BookType bookType) {
        return ResponseEntity.ok(bookTypeService.addBookType(bookType));
    }

    /**
     * Searches book types by query string.
     *
     * @param query search term
     * @return matching book types
     */
    @GetMapping("/search/{query}")
    public ResponseEntity<List<BookType>> searchBookType(@PathVariable String query) {
        return ResponseEntity.ok(bookTypeService.search(query));
    }
}