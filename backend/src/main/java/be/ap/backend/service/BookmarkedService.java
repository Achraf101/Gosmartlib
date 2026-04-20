package be.ap.backend.service;

import be.ap.backend.dto.BookmarkedDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Bookmarked;
import be.ap.backend.entity.User;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.BookmarkedRepository;
import be.ap.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BookmarkedService {

    private final BookmarkedRepository bookmarkedRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public List<BookmarkedDTO> getBookmarked(Long userId) {
        return bookmarkedRepository.findByUserIdOrderByAdded(userId).stream()
                .map(f -> new BookmarkedDTO(
                        f.getId(),
                        f.getBook().getId(),
                        f.getBook().getTitle(),
                        f.getBook().getCover(),
                        f.getBook().getAuthor(),
                        f.getAdded()))
                .toList();
    }

    public boolean isBookmarkedd(Long userId, Long bookId) {
        return bookmarkedRepository.existsByUserIdAndBookId(userId, bookId);
    }

    @Transactional
    public boolean toggleBookmarked(Long userId, Long bookId) {
        if (bookmarkedRepository.existsByUserIdAndBookId(userId, bookId)) {
            bookmarkedRepository.deleteByUserIdAndBookId(userId, bookId);
            return false;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));

        Bookmarked bookmarked = new Bookmarked();
        bookmarked.setUser(user);
        bookmarked.setBook(book);
        bookmarkedRepository.save(bookmarked);

        return true;
    }
}