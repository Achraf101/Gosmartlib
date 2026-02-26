package be.ap.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;

import be.ap.backend.entity.Book;
import be.ap.backend.service.BookService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;




@RestController
@RequestMapping("book")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService){
        this.bookService = bookService;
    }

    @GetMapping
    public @ResponseBody List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping(params = "id")
    public Book getMethodName(@RequestParam long id) {
        return bookService.getBookById(id);
    }
    

    @PostMapping
    public Book addBook(@RequestBody Book book) {
        return bookService.addBook(book);
    }
}