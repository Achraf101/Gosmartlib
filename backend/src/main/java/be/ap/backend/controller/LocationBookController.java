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
import be.ap.backend.dto.SchoolStatsDTO;
import be.ap.backend.service.LocationBookService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("locationbook")
@RequiredArgsConstructor
public class LocationBookController {

    private final LocationBookService locationBookService;

    @PostMapping
    public ResponseEntity<LocationBookDetailDTO> createLocationBook(@RequestBody LocationBookDTO dto) {
        return ResponseEntity.ok(locationBookService.createLocationBook(dto));
    }

    @GetMapping
    public ResponseEntity<List<LocationBookDetailDTO>> getAll() {
        return ResponseEntity.ok(locationBookService.findAll());
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<Page<LocationBookDetailDTO>> getByLocation(
            @PathVariable Long locationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(locationBookService.findByLocation(locationId, page, size));
    }

    @GetMapping("/{locationId}/books/{bookId}")
    public ResponseEntity<LocationBookDetailDTO> getLocationBook(
            @PathVariable Long locationId,
            @PathVariable Long bookId) {
        return ResponseEntity.ok(locationBookService.getLocationBook(locationId, bookId));
    }

    @GetMapping("/stats")
    public ResponseEntity<SchoolStatsDTO> getSchoolStats(HttpSession session) {
        Long schoolId = Long.valueOf(session.getAttribute("school").toString());
        return ResponseEntity.ok(locationBookService.getStatsForSchool(schoolId));
    }
}