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

import be.ap.backend.dto.LocationBookDTO;
import be.ap.backend.dto.LocationBookDetailDTO;
import be.ap.backend.dto.LocationStatsDTO;
import be.ap.backend.entity.LocationBook;
import be.ap.backend.exception.ArgumentsInvalidException;
import be.ap.backend.exception.BookAlreadyInLocationException;
import be.ap.backend.exception.MissingArgumentsException;
import be.ap.backend.service.LocationBookService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("locationbook")
@RequiredArgsConstructor
public class LocationBookController {
    private final LocationBookService locationBookService;

    @PostMapping
    public ResponseEntity<?> createLocationBook(@RequestBody LocationBookDTO dto) {
        try {
            LocationBook saved = locationBookService.createLocationBook(dto);
            return ResponseEntity.ok(saved);
        } catch (MissingArgumentsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ArgumentsInvalidException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (BookAlreadyInLocationException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    @GetMapping
    public List<LocationBookDetailDTO> getAll() {
        return locationBookService.findAll();
    }

    @GetMapping("/location/{locationId}")
    public Page<LocationBookDetailDTO> getByLocation(
            @PathVariable Long locationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return locationBookService.findByLocation(locationId, page, size);
    }

    @GetMapping("/{locationId}/books/{bookId}")
    public ResponseEntity<?> getLocationBook(
            @PathVariable Long locationId,
            @PathVariable Long bookId) {
        try {
            return ResponseEntity.ok(locationBookService.getLocationBook(locationId, bookId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<LocationStatsDTO> getLocationStats(HttpSession session) {
        Long locationId = Long.valueOf(session.getAttribute("location").toString());
        return ResponseEntity.ok(locationBookService.getStatsForLocation(locationId));
    }
}
