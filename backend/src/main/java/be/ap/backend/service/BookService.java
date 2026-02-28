package be.ap.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import be.ap.backend.entity.Book;
import be.ap.backend.repository.BookRepository;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book addBook(Book book) {
        return bookRepository.save(book);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(long id) {
        Optional<Book> book = bookRepository.findById(id);
        if (book.isPresent()) {
            return book.get();
        }
        return null;
    }

    public List<Book> getFeaturedBooks() {
        return bookRepository.findAll()
            .stream()
            .limit(4)
            .collect(Collectors.toList());
    }

    public List<Book> searchBooks(String query) {
        String q = query.toLowerCase();
        return bookRepository.findAll()
            .stream()
            .filter(book -> book.getTitle() != null && book.getTitle().toLowerCase().contains(q))
            .collect(Collectors.toList());
    }
}