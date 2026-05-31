package be.ap.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.BookCardDTO;
import be.ap.backend.dto.BookLookupDTO;
import be.ap.backend.dto.BookResultDTO;
import be.ap.backend.dto.CreateBookDTO;
import be.ap.backend.dto.UpdateBookDTO;
import be.ap.backend.entity.UserRole;
import be.ap.backend.enums.Clib;
import be.ap.backend.service.BookService;
import be.ap.backend.service.IsbnLookupService;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

/**
 * REST controller for managing books.
 *
 * <p>
 * Provides endpoints for retrieval, filtering, search, creation, updates,
 * and external ISBN lookup integration.
 * </p>
 */
@Validated
@RestController
@RequestMapping("book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final IsbnLookupService isbnLookupService;
    private final SessionContext sessionContext;

    /**
     * Retrieves a paginated list of books.
     *
     * @param full     optional flag to request full representation
     * @param location optional location filter
     * @param page     page index (zero-based)
     * @param size     page size
     * @return paginated list of books
     */
    @GetMapping
    public ResponseEntity<Page<BookResultDTO>> getAll(
            @RequestParam(required = false) Boolean full,
            @RequestParam(required = false) Long location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(bookService.getAll(location, full, PageRequest.of(page, size)));
    }

    /**
     * Retrieves a book by its ID.
     *
     * @param id book identifier
     * @return book details
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookResultDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getById(id));
    }

    /**
     * Searches books by keyword.
     *
     * @param query search term
     * @param page  page index
     * @param size  page size
     * @return paginated search results
     */
    @GetMapping("/search/{query}")
    public ResponseEntity<Page<BookResultDTO>> search(
            @PathVariable String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(bookService.search(query, PageRequest.of(page, size)));
    }

    /**
     * Retrieves books related to a given book.
     *
     * @param id book identifier
     * @return list of related books
     */
    @GetMapping("/{id}/related")
    public ResponseEntity<List<BookCardDTO>> getRelated(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getRelated(id));
    }

    /**
     * Filters books using multiple optional criteria.
     *
     * <p>
     * Non-admin users are restricted to their school context.
     * </p>
     *
     * @param query     search query
     * @param location  location filter
     * @param genres    genre IDs
     * @param language  language ID
     * @param fiction   fiction flag
     * @param authorIds author IDs
     * @param seriesIds series IDs
     * @param pagesMin  minimum page count
     * @param pagesMax  maximum page count
     * @param clibs     classification levels
     * @param themes    theme IDs
     * @param didactic  didactic filter
     * @param page      page index
     * @param size      page size
     * @return filtered paginated results
     */
    @GetMapping("/filter")
    public ResponseEntity<Page<BookResultDTO>> filter(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long location,
            @RequestParam(required = false) List<Long> genres,
            @RequestParam(required = false) Long language,
            @RequestParam(required = false) Boolean fiction,
            @RequestParam(required = false) List<Long> authorIds,
            @RequestParam(required = false) List<Long> seriesIds,
            @RequestParam(required = false) @Min(0) Integer pagesMin,
            @RequestParam(required = false) @Min(0) Integer pagesMax,
            @RequestParam(required = false) List<Clib> clibs,
            @RequestParam(required = false) List<Long> themes,
            @RequestParam(required = false) Boolean didactic,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        boolean isAdmin = sessionContext.hasRole(UserRole.ADMIN);
        Long schoolId = isAdmin ? null : sessionContext.getSchoolId();

        return ResponseEntity.ok(bookService.filter(schoolId, isAdmin, location, genres, language, fiction,
                authorIds, seriesIds, pagesMin, pagesMax, clibs, themes, didactic, query,
                PageRequest.of(page, size)));
    }

    /**
     * Creates a new book.
     *
     * @param dto book creation payload
     * @return created book
     */
    @PostMapping
    public ResponseEntity<BookResultDTO> addBook(@RequestBody CreateBookDTO dto) {
        return ResponseEntity.ok(bookService.saveBook(dto));
    }

    /**
     * Looks up a book using its ISBN via external integration.
     *
     * @param isbn ISBN identifier
     * @return book metadata if found
     * @throws EntityNotFoundException if no book is found for the ISBN
     */
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<BookLookupDTO> lookupByIsbn(@PathVariable String isbn) {
        return isbnLookupService.lookup(isbn)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("Geen boek gevonden met ISBN: " + isbn));
    }

    /**
     * Retrieves AI-generated preview data for a book.
     *
     * @param id book identifier
     * @return key-value preview data
     */
    @GetMapping("/{id}/ia-preview")
    public ResponseEntity<Map<String, String>> getIaPreview(@PathVariable Long id) {
        return bookService.getIaPreview(id);
    }

    /**
     * Retrieves paginated book results (alternative listing endpoint).
     *
     * @param page page index
     * @param size page size
     * @return paginated book results
     */
    @GetMapping("/bookResult")
    public ResponseEntity<Page<BookResultDTO>> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(bookService.getAllBookResults(PageRequest.of(page, size)));
    }

    /**
     * Updates an existing book.
     *
     * @param id  book identifier
     * @param dto update payload
     * @return updated book
     */
    @PutMapping("/{id}")
    public ResponseEntity<BookResultDTO> updateBook(@PathVariable Long id, @RequestBody UpdateBookDTO dto) {
        return ResponseEntity.ok(bookService.updateBook(id, dto));
    }
}