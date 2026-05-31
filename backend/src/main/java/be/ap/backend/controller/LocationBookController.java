package be.ap.backend.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.LocationAvailabilityDTO;
import be.ap.backend.dto.LocationBookDTO;
import be.ap.backend.dto.LocationBookDetailDTO;
import be.ap.backend.dto.SchoolStatsDTO;
import be.ap.backend.exception.MissingSessionException;
import be.ap.backend.service.LocationBookService;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing location-book relations.
 * Handles book inventory per location and school-level availability/statistics.
 */
@RestController
@RequestMapping("locationbook")
@RequiredArgsConstructor
public class LocationBookController {

    private final LocationBookService locationBookService;
    private final SessionContext sessionContext;

    /**
     * Creates a new location-book relation.
     *
     * @param dto location-book data
     * @return created location-book details
     */
    @PostMapping
    public ResponseEntity<LocationBookDetailDTO> createLocationBook(@RequestBody LocationBookDTO dto) {
        return ResponseEntity.ok(locationBookService.createLocationBook(dto));
    }

    /**
     * Retrieves all location-book entries.
     *
     * @return list of all location-book details
     */
    @GetMapping
    public ResponseEntity<List<LocationBookDetailDTO>> getAll() {
        return ResponseEntity.ok(locationBookService.findAll());
    }

    /**
     * Retrieves paginated books for a specific location.
     *
     * @param locationId location id
     * @param page       page index
     * @param size       page size
     * @return paginated list of location-book details
     */
    @GetMapping("/location/{locationId}")
    public ResponseEntity<Page<LocationBookDetailDTO>> getByLocation(
            @PathVariable Long locationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(locationBookService.findByLocation(locationId, page, size));
    }

    /**
     * Retrieves a specific book in a specific location.
     *
     * @param locationId location id
     * @param bookId     book id
     * @return location-book detail
     */
    @GetMapping("/{locationId}/books/{bookId}")
    public ResponseEntity<LocationBookDetailDTO> getLocationBook(
            @PathVariable Long locationId,
            @PathVariable Long bookId) {
        return ResponseEntity.ok(locationBookService.getLocationBook(locationId, bookId));
    }

    /**
     * Retrieves availability of a book across locations for the current school.
     *
     * @param bookId book id
     * @return list of availability per location
     */
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<LocationAvailabilityDTO>> getAvailabilityByBook(
            @PathVariable Long bookId) {
        Object raw = sessionContext.getSchoolId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long schoolId = Long.valueOf(raw.toString());
        return ResponseEntity.ok(locationBookService.getAvailabilityByBook(bookId, schoolId));
    }

    /**
     * Retrieves aggregated statistics for the current school.
     *
     * @return school statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<SchoolStatsDTO> getSchoolStats() {
        Object raw = sessionContext.getSchoolId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long schoolId = Long.valueOf(raw.toString());
        return ResponseEntity.ok(locationBookService.getStatsForSchool(schoolId));
    }
}