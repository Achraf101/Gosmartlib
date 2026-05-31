package be.ap.backend.controller;

import be.ap.backend.dto.SectionBookDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Section;
import be.ap.backend.service.SectionService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing sections and their associated books.
 *
 * <p>
 * A section represents a grouping of books within a school context.
 * This controller provides endpoints for retrieving section data,
 * managing section books, and configuring spotlight and grade-based selections.
 * </p>
 *
 * <p>
 * Business logic is delegated to {@link SectionService}.
 * </p>
 */
@RestController
@RequestMapping("section")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    /**
     * Retrieves all sections for a given school.
     *
     * @param schoolId the school identifier
     * @return list of sections
     */
    @GetMapping
    public List<Section> getAllSections(@RequestParam Long schoolId) {
        return sectionService.getAllSections(schoolId);
    }

    /**
     * Retrieves all books assigned to a section.
     *
     * @param id section ID
     * @return list of books in the section
     */
    @GetMapping("/{id}/books")
    public List<Book> getBooksBySection(@PathVariable Long id) {
        return sectionService.getBooksBySection(id);
    }

    /**
     * Retrieves a book in a section filtered by grade.
     *
     * @param id    section ID
     * @param grade grade filter
     * @return matching book
     */
    @GetMapping("/{id}/books/grade")
    public Book getBookBySectionAndGrade(
            @PathVariable Long id,
            @RequestParam Byte grade) {
        return sectionService.getBookBySectionAndGrade(id, grade);
    }

    /**
     * Assigns or updates the "book of the month" for a section.
     *
     * @param id     section ID
     * @param bookId book ID
     * @param grade  grade level
     * @return updated book assignment
     */
    @PutMapping("/{id}/book")
    public Book setBookOfMonth(
            @PathVariable Long id,
            @RequestParam Long bookId,
            @RequestParam Byte grade) {
        return sectionService.setBookOfMonth(id, bookId, grade);
    }

    /**
     * Sets a spotlight book for a section with ranking priority.
     *
     * @param sectionId section ID
     * @param bookId    book ID
     * @param ranking   display ranking
     * @return updated spotlight entry
     */
    @PutMapping("/{sectionId}/spotlight")
    public ResponseEntity<?> setSpotlightBook(
            @PathVariable Long sectionId,
            @RequestParam Long bookId,
            @RequestParam Short ranking) {
        return ResponseEntity.ok(sectionService.setSpotlightBook(sectionId, bookId, ranking));
    }

    /**
     * Retrieves spotlight books for a section.
     *
     * @param id section ID
     * @return list of spotlight books
     */
    @GetMapping("/{id}/spotlight")
    public ResponseEntity<List<SectionBookDTO>> getSpotlightBooks(@PathVariable Long id) {
        return ResponseEntity.ok(sectionService.getSpotlightBooks(id));
    }
}