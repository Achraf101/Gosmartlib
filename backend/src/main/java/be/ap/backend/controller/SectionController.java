package be.ap.backend.controller;

import be.ap.backend.dto.SectionBookDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Section;
import be.ap.backend.service.SectionService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("section")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @GetMapping
    public List<Section> getAllSections(@RequestParam Long schoolId) {
        return sectionService.getAllSections(schoolId);
    }

    @GetMapping("/{id}/books")
    public List<Book> getBooksBySection(@PathVariable Long id) {
        return sectionService.getBooksBySection(id);
    }

    @GetMapping("/{id}/books/grade")
    public Book getBookBySectionAndGrade(
            @PathVariable Long id,
            @RequestParam Byte grade) {
        return sectionService.getBookBySectionAndGrade(id, grade);
    }

    @PutMapping("/{id}/book")
    public Book setBookOfMonth(
            @PathVariable Long id,
            @RequestParam Long bookId,
            @RequestParam Byte grade) {
        return sectionService.setBookOfMonth(id, bookId, grade);
    }

    @PutMapping("/{sectionId}/spotlight")
    public ResponseEntity<?> setSpotlightBook(
        @PathVariable Long sectionId,
        @RequestParam Long bookId,
        @RequestParam Short ranking) {
        return ResponseEntity.ok(sectionService.setSpotlightBook(sectionId, bookId, ranking));
    }

    @GetMapping("/{id}/spotlight")
    public ResponseEntity<List<SectionBookDTO>> getSpotlightBooks(@PathVariable Long id) {
        return ResponseEntity.ok(sectionService.getSpotlightBooks(id));
    }
}