package be.ap.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.entity.BookType;
import be.ap.backend.repository.BookTypeRepository;

@RestController
@RequestMapping("booktype")
public class BookTypeController {

    private final BookTypeRepository bookTypeRepository;

    public BookTypeController(BookTypeRepository bookTypeRepository) {
        this.bookTypeRepository = bookTypeRepository;
    }

    @GetMapping()
    public List<BookType> getAll() {
        return bookTypeRepository.findAll();
    }

    @PostMapping()
    public BookType addBookType(@RequestBody BookType bookType) {
        return bookTypeRepository.save(bookType);
    }
}
