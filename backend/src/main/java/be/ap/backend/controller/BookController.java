package be.ap.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.dto.CreateBookDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.service.BookService;

@RestController
@RequestMapping("book")
public class BookController {

    private final BookRepository bookRepository;
    private final BookService bookService;

    public BookController(BookRepository bookRepository, BookService bookService) {
        this.bookRepository = bookRepository;
        this.bookService = bookService;
    }

    /**
     * Gives all the books
     * 
     * @return List of books
     */
    @GetMapping()
    public List<Book> getAll() {
        return bookRepository.findBy();
    }

    @GetMapping("/{id}")
    public Book getById(@PathVariable Long id){
        return bookRepository.findById(id).orElse(null);
    }

    /**
     * Saves a book via the dto
     * 
     * @param dto Book dto
     * @return ReponseEntity of type Book
     */
    @PostMapping()
    public Book addBook(@RequestBody CreateBookDTO dto) {
        Book savedBook = bookService.saveBook(dto);
        return savedBook;
    }
}
