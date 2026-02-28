package be.ap.backend.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import be.ap.backend.entity.Book;
import be.ap.backend.service.BookService;

@Controller
@RequestMapping("book")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public @ResponseBody List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping(params = "id")
    public @ResponseBody Book getBookById(@RequestParam long id) {
        return bookService.getBookById(id);
    }

    @PostMapping
    public @ResponseBody Book addBook(@RequestBody Book book) {
        return bookService.addBook(book);
    }

    @GetMapping("/featured")
    public @ResponseBody List<Book> getFeaturedBooks() {
        return bookService.getFeaturedBooks();
    }

    @GetMapping("/search")
    public @ResponseBody List<Book> searchBooks(@RequestParam String q) {
        return bookService.searchBooks(q);
    }
}