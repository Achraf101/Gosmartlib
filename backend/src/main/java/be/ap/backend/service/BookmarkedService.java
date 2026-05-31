package be.ap.backend.service;

import be.ap.backend.dto.BookmarkedDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Bookmarked;
import be.ap.backend.entity.User;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.BookmarkedRepository;
import be.ap.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing bookmarked books per user.
 */
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

    public boolean isBookmarked(Long userId, Long bookId) {
        return bookmarkedRepository.existsByUserIdAndBookId(userId, bookId);
    }

    /**
     * Adds or removes the bookmark for the given user and book.
     *
     * @return {@code true} if the book was bookmarked, {@code false} if it was
     *         removed
     * @throws EntityNotFoundException if the user or book does not exist
     */
    @Transactional
    public boolean toggleBookmarked(Long userId, Long bookId) {
        if (bookmarkedRepository.existsByUserIdAndBookId(userId, bookId)) {
            bookmarkedRepository.deleteByUserIdAndBookId(userId, bookId);
            return false;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Gebruiker niet gevonden met id: " + userId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Boek niet gevonden met id: " + bookId));

        Bookmarked bookmarked = new Bookmarked();
        bookmarked.setUser(user);
        bookmarked.setBook(book);
        bookmarkedRepository.save(bookmarked);

        return true;
    }
}