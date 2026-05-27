package be.ap.backend.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import be.ap.backend.dto.SharedListResponseDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.BookList;
import be.ap.backend.entity.BookListItem;
import be.ap.backend.repository.BookListItemRepository;
import be.ap.backend.repository.BookListRepository;
import be.ap.backend.repository.SavedListRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookListService {

    private final BookListRepository bookListRepository;
    private final BookListItemRepository bookListItemRepository;
    private final SavedListRepository savedListRepository;

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";
    private static final int TOKEN_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    @PersistenceContext
    private EntityManager entityManager;

    private String generateToken() {
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return token.toString();
    }

    private String generateUniqueToken() {
        String token;
        do {
            token = generateToken();
        } while (bookListRepository.findByShareToken(token).isPresent());
        return token;
    }

    public BookList generateShareToken(Long userId, Long listId) {
        BookList list = bookListRepository.findById(listId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "List not found"));
        if (!list.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This is not your list");
        }
        if (list.getShareToken() == null) {
            list.setShareToken(generateUniqueToken());
            bookListRepository.save(list);
        }
        return list;
    }

    public BookList createList(Long ownerId, String name) {
        BookList list = new BookList();
        list.setOwnerId(ownerId);
        list.setName(name);
        list.setShareToken(null);
        list.setCreatedAt(LocalDateTime.now());
        return bookListRepository.save(list);
    }

    public List<BookList> getListsByOwner(Long ownerId) {
        return bookListRepository.findByOwnerId(ownerId);
    }

    public Optional<BookList> getListById(Long listId) {
        return bookListRepository.findById(listId);
    }

    public Optional<BookList> getListByToken(String token) {
        return bookListRepository.findByShareToken(token);
    }

    public BookList renameList(Long listId, String newName) {
        BookList list = bookListRepository.findById(listId)
                .orElseThrow(() -> new EntityNotFoundException("Lijst niet gevonden met id: " + listId));
        list.setName(newName);
        return bookListRepository.save(list);
    }

    public void deleteList(Long listId) {
        bookListRepository.deleteById(listId);
    }

    public BookListItem addBook(Long listId, Long bookId) {
        BookListItem item = new BookListItem();
        item.setBookListId(listId);
        item.setBookId(bookId);
        return bookListItemRepository.save(item);
    }

    public List<Book> getBooksInList(Long listId) {
        return bookListItemRepository.findByBookListId(listId)
            .stream()
            .map(item -> entityManager.find(Book.class, item.getBookId()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public void removeBook(Long bookId, Long listId) {
        bookListItemRepository.deleteByBookIdAndBookListId(bookId, listId);
    }

    public List<BookList> getListsWithoutBook(Long userId, Long bookId) {
        return bookListRepository.findListsWithoutBook(userId, bookId);
    }

    public SharedListResponseDTO getSharedList(String token) {
        BookList list = bookListRepository.findByShareToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "List not found"));
        List<Book> books = bookListItemRepository.findByBookListId(list.getId())
                .stream()
                .map(item -> entityManager.find(Book.class, item.getBookId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new SharedListResponseDTO(list, books);
    }

    public BookList removeShareToken(Long userId, Long listId) {
        BookList list = bookListRepository.findById(listId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "List not found"));
        if (!list.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This is not your list");
        }
        list.setShareToken(null);
        savedListRepository.deleteByBookListId(listId); // remove from all saved lists
        return bookListRepository.save(list);
    }
}
