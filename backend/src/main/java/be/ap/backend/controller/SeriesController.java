package be.ap.backend.controller;

import be.ap.backend.dto.SeriesDTO;
import be.ap.backend.service.SeriesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing book series.
 *
 * <p>
 * A series groups books under a shared title and optional author.
 * This controller exposes basic CRUD-like operations and search functionality.
 * </p>
 *
 * <p>
 * All business logic is delegated to {@link SeriesService}.
 * </p>
 */
@RestController
@RequestMapping("series")
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesService seriesService;

    /**
     * Retrieves all series.
     *
     * @return list of series
     */
    @GetMapping
    public List<SeriesDTO> getAll() {
        return seriesService.findAll();
    }

    /**
     * Searches series by name.
     *
     * @param name search term
     * @return matching series
     */
    @GetMapping("/search/{name}")
    public List<SeriesDTO> search(@PathVariable String name) {
        return seriesService.searchByName(name);
    }

    /**
     * Retrieves a series by ID.
     *
     * @param id series ID
     * @return the matching series
     */
    @GetMapping("/{id}")
    public ResponseEntity<SeriesDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(seriesService.findById(id));
    }

    /**
     * Creates a new series.
     *
     * @param dto series data
     * @return created series
     */
    @PostMapping
    public ResponseEntity<SeriesDTO> create(@RequestBody SeriesDTO dto) {
        return ResponseEntity.ok(seriesService.createSeries(dto));
    }
}