package be.ap.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.entity.Publisher;
import be.ap.backend.service.PublisherService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * REST controller for managing {@link Publisher} entities.
 * <p>
 * Provides endpoints to retrieve, search, and create publishers.
 * </p>
 */
@RestController
@RequestMapping("publisher")
@RequiredArgsConstructor
public class PublisherController {

    private final PublisherService publisherService;

    /**
     * Retrieves all publishers.
     *
     * @return list of all publishers
     */
    @GetMapping
    public ResponseEntity<List<Publisher>> getAll() {
        return ResponseEntity.ok(publisherService.getAll());
    }

    /**
     * Searches publishers by a free-text query.
     *
     * @param query search term used to filter publishers
     * @return list of matching publishers
     */
    @GetMapping("/search/{query}")
    public ResponseEntity<List<Publisher>> search(@PathVariable String query) {
        return ResponseEntity.ok(publisherService.search(query));
    }

    /**
     * Creates a new publisher.
     *
     * @param publisher publisher payload to persist
     * @return the created publisher
     */
    @PostMapping
    public ResponseEntity<Publisher> addPublisher(@RequestBody Publisher publisher) {
        return ResponseEntity.ok(publisherService.addPublisher(publisher));
    }
}