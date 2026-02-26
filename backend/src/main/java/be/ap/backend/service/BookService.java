package be.ap.backend.service;

import be.ap.backend.mapper.BookMapper;
import be.ap.backend.model.BookDTO;
import be.ap.backend.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookService(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll()
            .stream()
            .map(book -> bookMapper.toDTO(book, null, null))
            .collect(Collectors.toList());
    }

    public List<BookDTO> getFeaturedBooks() {
        return bookRepository.findAll()
            .stream()
            .limit(4)
            .map(book -> bookMapper.toDTO(book, null, null))
            .collect(Collectors.toList());
    }

    public List<BookDTO> searchBooks(String query) {
        String q = query.toLowerCase();
        return bookRepository.findAll()
            .stream()
            .filter(book -> book.getTitle() != null && book.getTitle().toLowerCase().contains(q))
            .map(book -> bookMapper.toDTO(book, null, null))
            .collect(Collectors.toList());
    }
}
