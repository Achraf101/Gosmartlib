package be.ap.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.validation.annotation.Validated;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import be.ap.backend.dto.BookCardDTO;
import be.ap.backend.dto.BookLookupDTO;
import be.ap.backend.dto.BookResultDTO;
import be.ap.backend.dto.CreateBookDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Clib;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.service.BookService;
import be.ap.backend.service.IsbnLookupService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    // return books only on selected campus
    @GetMapping
    public Page<Book> getAll(
            HttpSession session,
            @RequestParam(required = false) Boolean full,
            @RequestParam(required = false) Long campus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);

        List<Long> ids = getLocationIds(session);

        if (full == true) {
            System.out.println("all books being returned");
            return bookRepository.findAll(pageable);
        } else if (ids.contains(campus) || campus == null) {
            System.out.println("all books from a campus being returned");
            return bookRepository.findAllByCampus(ids, pageable);
        } else {
            return Page.empty(pageable);
        }
    }

    @GetMapping("/{id}")
    public Book getById(@PathVariable Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    @GetMapping("/search/{query}")
    public Page<Book> search(
            HttpSession session,
            @PathVariable String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        List<Long> ids = getLocationIds(session);
        Pageable pageable = PageRequest.of(page, size);

        return bookRepository.search(ids, query, pageable);
    }

    @GetMapping("/{id}/related")
    public List<BookCardDTO> getRelated(HttpSession session, @PathVariable Long id) {
        return bookRepository.findRelated(id, getLocationIds(session));
    }

    @GetMapping("/filter")
    public Page<Book> filter(
            HttpSession session,
            @RequestParam(required = false) Long campus,
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

        if (getLocationIds(session).contains(campus)) {
            return bookService.filter(campus, genres, language, fiction, authorIds, seriesIds, pagesMin, pagesMax,
                    clibs,
                    themes,
                    didactic,
                    pageable);
        } else {
            return Page.empty(pageable);
        }

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

    @GetMapping("/bookResult")
    public Page<BookResultDTO> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return bookService.getAllBookResults(pageable);
    }

    List<Long> getLocationIds(HttpSession session) {
        final String campusString = (String) session.getAttribute("campus");

        // transform to array "[1, 2]" -> [1, 2]
        return Arrays.stream(campusString.replaceAll("[\\[\\]\\s]", "").split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}