package be.ap.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.entity.Author;
import be.ap.backend.service.AuthorService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing authors.
 *
 * <p>
 * Provides endpoints for retrieving, searching, and creating authors.
 * </p>
 */
@RestController
@RequestMapping("author")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    /**
     * Retrieves all authors.
     *
     * @return list of all authors
     */
    @GetMapping
    public ResponseEntity<List<Author>> getAll() {
        return ResponseEntity.ok(authorService.getAll());
    }

    /**
     * Retrieves a single author by ID.
     *
     * @param id author identifier
     * @return the requested author
     */
    @GetMapping("/{id}")
    public ResponseEntity<Author> getById(@PathVariable Long id) {
        return ResponseEntity.ok(authorService.getById(id));
    }

    /**
     * Searches authors by name or keyword.
     *
     * @param query search term
     * @return list of matching authors
     */
    @GetMapping("/search/{query}")
    public ResponseEntity<List<Author>> searchAuthor(@PathVariable String query) {
        return ResponseEntity.ok(authorService.search(query));
    }

    /**
     * Creates a new author.
     *
     * @param author author payload
     * @return the created author
     */
    @PostMapping
    public ResponseEntity<Author> addAuthor(@RequestBody Author author) {
        return ResponseEntity.ok(authorService.addAuthor(author));
    }
}