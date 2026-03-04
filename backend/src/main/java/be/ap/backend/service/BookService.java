package be.ap.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import be.ap.backend.entity.Book;
import be.ap.backend.repository.BookRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
}