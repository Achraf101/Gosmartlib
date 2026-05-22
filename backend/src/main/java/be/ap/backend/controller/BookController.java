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
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import be.ap.backend.dto.BookCardDTO;
import be.ap.backend.dto.BookLookupDTO;
import be.ap.backend.dto.BookResultDTO;
import be.ap.backend.dto.CreateBookDTO;
import be.ap.backend.dto.UpdateBookDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Clib;
import be.ap.backend.service.BookService;
import be.ap.backend.service.IsbnLookupService;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;

@Validated
@RestController
@RequestMapping("book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final IsbnLookupService isbnLookupService;

    @GetMapping
    public ResponseEntity<Page<Book>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(bookService.getAll(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getById(id));
    }

    @GetMapping("/search/{query}")
    public ResponseEntity<Page<Book>> search(
            @PathVariable String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(bookService.search(query, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<List<BookCardDTO>> getRelated(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getRelated(id));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<Book>> filter(
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
        return ResponseEntity.ok(bookService.filter(genres, language, fiction, authorIds, seriesIds, pagesMin, pagesMax, clibs, themes, didactic, PageRequest.of(page, size)));
    }

    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody CreateBookDTO dto) {
        return ResponseEntity.ok(bookService.saveBook(dto));
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<BookLookupDTO> lookupByIsbn(@PathVariable String isbn) {
        return isbnLookupService.lookup(isbn)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/bookResult")
    public ResponseEntity<Page<BookResultDTO>> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(bookService.getAllBookResults(PageRequest.of(page, size)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody UpdateBookDTO dto) {
        return ResponseEntity.ok(bookService.updateBook(id, dto));
    }
}