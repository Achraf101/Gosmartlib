package be.ap.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import be.ap.backend.entity.BookType;
import be.ap.backend.repository.BookTypeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookTypeService {

    private final BookTypeRepository bookTypeRepository;

    public List<BookType> getAll() {
        return bookTypeRepository.findAll();
    }

    public BookType addBookType(BookType bookType) {
        return bookTypeRepository.save(bookType);
    }

    public List<BookType> search(String query) {
        return bookTypeRepository.findByNameContainingIgnoreCase(query);
    }
}