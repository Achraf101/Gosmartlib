package be.ap.backend.controller;

import be.ap.backend.model.BookDTO;
import be.ap.backend.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@CrossOrigin(origins = "*")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // GET /api/books — alle boeken
    @GetMapping
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    // GET /api/books/featured — eerste 4 boeken voor startscherm
    @GetMapping("/featured")
    public ResponseEntity<List<BookDTO>> getFeaturedBooks() {
        return ResponseEntity.ok(bookService.getFeaturedBooks());
    }

    // GET /api/books/search?q=harry
    @GetMapping("/search")
    public ResponseEntity<List<BookDTO>> searchBooks(@RequestParam String q) {
        if (q == null || q.isBlank()) {
            return ResponseEntity.ok(bookService.getAllBooks());
        }
        return ResponseEntity.ok(bookService.searchBooks(q));
    }
}
