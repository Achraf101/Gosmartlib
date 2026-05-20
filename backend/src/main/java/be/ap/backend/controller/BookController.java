package be.ap.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import be.ap.backend.dto.BookCardDTO;
import be.ap.backend.dto.BookLookupDTO;
import be.ap.backend.dto.BookResultDTO;
import be.ap.backend.dto.CreateBookDTO;
import be.ap.backend.dto.UpdateBookDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Clib;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.service.BookService;
import be.ap.backend.service.IsbnLookupService;
import be.ap.backend.service.OpenLibraryService;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

@Validated
@RestController
@RequestMapping("book")
@RequiredArgsConstructor
public class BookController {

    private final BookRepository bookRepository;
    private final BookService bookService;
    private final IsbnLookupService isbnLookupService;
    private final OpenLibraryService openLibraryService;

    @GetMapping
    public Page<Book> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findAll(pageable);
    }

    @GetMapping("/{id}")
    public Book getById(@PathVariable Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    @GetMapping("/search/{query}")
    public Page<Book> search(
            @PathVariable String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.search(query, pageable);
    }

    @GetMapping("/{id}/related")
    public List<BookCardDTO> getRelated(@PathVariable Long id) {
        return bookRepository.findRelated(id);
    }

    @GetMapping("/filter")
    public Page<Book> filter(
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

        if (pagesMin != null && pagesMax != null && pagesMin > pagesMax) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pagesMin moet kleiner zijn dan pagesMax");
        }

        Pageable pageable = PageRequest.of(page, size);
        return bookService.filter(genres, language, fiction, authorIds, seriesIds, pagesMin, pagesMax, clibs, themes,
                didactic,
                pageable);
    }

    @PostMapping
    public Book addBook(@RequestBody CreateBookDTO dto) {
        return bookService.saveBook(dto);
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<BookLookupDTO> lookupByIsbn(@PathVariable String isbn) {
        return isbnLookupService.lookup(isbn)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/ia-preview")
    public ResponseEntity<Map<String, String>> getIaPreview(@PathVariable Long id) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null || book.getIsbn() == null) {
            return ResponseEntity.notFound().build();
        }
        return openLibraryService.getIaIdentifier(book.getIsbn())
                .map(iaId -> ResponseEntity.ok(Map.of("ia_id", iaId)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/bookResult")
    public Page<BookResultDTO> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return bookService.getAllBookResults(pageable);
    }

    @PutMapping("/{id}")
    public Book updateBook(@PathVariable Long id, @RequestBody UpdateBookDTO dto) {
        return bookService.updateBook(id, dto);
    }
}