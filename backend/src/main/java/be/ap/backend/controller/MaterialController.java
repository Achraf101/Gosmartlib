package be.ap.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.entity.Material;
import be.ap.backend.service.MaterialService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * REST controller for managing {@link Material} resources.
 * <p>
 * Provides endpoints to retrieve materials associated with a specific book.
 * </p>
 */
@RestController
@RequestMapping("material")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    /**
     * Retrieves all materials linked to a given book.
     *
     * @param bookId the ID of the book whose materials should be retrieved
     * @return a {@link ResponseEntity} containing a list of {@link Material}
     *         objects
     */
    @GetMapping("/{bookId}")
    public ResponseEntity<List<Material>> getMaterial(@PathVariable Long bookId) {
        return ResponseEntity.ok(materialService.getByBookId(bookId));
    }
}
