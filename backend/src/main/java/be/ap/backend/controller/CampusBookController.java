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

import be.ap.backend.dto.CampusBookDTO;
import be.ap.backend.dto.CampusBookDetailDTO;
import be.ap.backend.dto.CampusStatsDTO;
import be.ap.backend.entity.CampusBook;
import be.ap.backend.exception.ArgumentsInvalidException;
import be.ap.backend.exception.BookAlreadyInCampusException;
import be.ap.backend.exception.MissingArgumentsException;
import be.ap.backend.service.CampusBookService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("campusbook")
@RequiredArgsConstructor
public class CampusBookController {
    private final CampusBookService campusBookService;

    @PostMapping
    public ResponseEntity<?> createCampusBook(@RequestBody CampusBookDTO dto) {
        try {
            CampusBook saved = campusBookService.createCampusBook(dto);
            return ResponseEntity.ok(saved);
        } catch (MissingArgumentsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ArgumentsInvalidException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (BookAlreadyInCampusException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    @GetMapping
    public List<CampusBookDetailDTO> getAll() {
        return campusBookService.findAll();
    }

    @GetMapping("/campus/{campusId}")
    public Page<CampusBookDetailDTO> getByCampus(
            @PathVariable Long campusId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return campusBookService.findByCampus(campusId, page, size);
    }

    @GetMapping("/{campusId}/books/{bookId}")
    public ResponseEntity<?> getCampusBook(
            @PathVariable Long campusId,
            @PathVariable Long bookId) {
        try {
            return ResponseEntity.ok(campusBookService.getCampusBook(campusId, bookId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<CampusStatsDTO> getCampusStats(HttpSession session) {
        Long campusId = Long.valueOf(session.getAttribute("campus").toString());
        return ResponseEntity.ok(campusBookService.getStatsForCampus(campusId));
    }
}
