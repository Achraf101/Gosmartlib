package be.ap.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.entity.Genre;
import be.ap.backend.service.GenreService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * REST controller for managing book genres.
 * Provides endpoints to retrieve, search, and create genres.
 */
@RestController
@RequestMapping("genre")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    /**
     * Retrieves all available genres.
     *
     * @return list of all genres
     */
    @GetMapping
    public ResponseEntity<List<Genre>> getAll() {
        return ResponseEntity.ok(genreService.getAll());
    }

    /**
     * Searches genres by a query string.
     *
     * @param query search term used to filter genres
     * @return list of matching genres
     */
    @GetMapping("/search/{query}")
    public ResponseEntity<List<Genre>> search(@PathVariable String query) {
        return ResponseEntity.ok(genreService.search(query));
    }

    /**
     * Creates a new genre.
     *
     * @param genre genre object to persist
     * @return the created genre
     */
    @PostMapping
    public ResponseEntity<Genre> addGenre(@RequestBody Genre genre) {
        return ResponseEntity.ok(genreService.addGenre(genre));
    }
}