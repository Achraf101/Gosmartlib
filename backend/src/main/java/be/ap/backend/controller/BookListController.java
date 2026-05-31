package be.ap.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

import be.ap.backend.entity.Book;
import be.ap.backend.entity.BookList;
import be.ap.backend.entity.BookListItem;
import be.ap.backend.exception.MissingSessionException;
import be.ap.backend.service.BookListService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.SharedListResponseDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing user book lists.
 *
 * <p>
 * Supports creating, updating, deleting, sharing, and retrieving
 * personal and shared book lists, as well as managing list contents.
 * </p>
 */
@RestController
@RequestMapping("lists")
@RequiredArgsConstructor
public class BookListController {

    private final BookListService bookListService;
    private final SessionContext sessionContext;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Creates a new book list for the currently authenticated user.
     *
     * @param request contains the list name
     * @return created book list
     * @throws MissingSessionException if the user is not logged in
     */
    @PostMapping
    public ResponseEntity<BookList> createList(@RequestBody CreateListRequest request) {
        Object raw = sessionContext.getUserId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long userId = Long.valueOf(raw.toString());
        BookList list = bookListService.createList(userId, request.getName());
        return ResponseEntity.ok(list);
    }

    /**
     * Retrieves all book lists owned by the currently authenticated user.
     *
     * @return list of user-owned book lists
     * @throws MissingSessionException if the user is not logged in
     */
    @GetMapping
    public ResponseEntity<List<BookList>> getMyLists() {
        Object raw = sessionContext.getUserId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long userId = Long.valueOf(raw.toString());
        return ResponseEntity.ok(bookListService.getListsByOwner(userId));
    }

    /**
     * Adds a book to a specific book list.
     *
     * @param listId  target list ID
     * @param request contains book ID to add
     * @return created list item entry
     */
    @PostMapping("/{listId}/books")
    public ResponseEntity<BookListItem> addBook(@PathVariable Long listId,
            @RequestBody AddBookRequest request) {
        BookListItem book = bookListService.addBook(listId, request.getBookId());
        return ResponseEntity.ok(book);
    }

    /**
     * Removes a book from a book list.
     *
     * @param listId target list ID
     * @param bookId book identifier to remove
     * @return no content response
     */
    @DeleteMapping("/{listId}/books/{bookId}")
    public ResponseEntity<Void> removeBook(@PathVariable Long bookId, @PathVariable Long listId) {
        bookListService.removeBook(bookId, listId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Renames an existing book list.
     *
     * @param listId  list identifier
     * @param request contains new name
     * @return updated book list
     */
    @PutMapping("/{listId}")
    public ResponseEntity<BookList> renameList(@PathVariable Long listId,
            @RequestBody CreateListRequest request) {
        BookList list = bookListService.renameList(listId, request.getName());
        return ResponseEntity.ok(list);
    }

    /**
     * Deletes a book list.
     *
     * @param listId list identifier
     * @return no content response
     */
    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> deleteList(@PathVariable Long listId) {
        bookListService.deleteList(listId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves a shared book list using a share token.
     *
     * @param token share token
     * @return shared list representation
     */
    @GetMapping("/shared/{token}")
    public ResponseEntity<SharedListResponseDTO> getSharedList(@PathVariable String token) {
        return ResponseEntity.ok(bookListService.getSharedList(token));
    }

    /**
     * Retrieves all books contained in a specific book list.
     *
     * @param listId list identifier
     * @return list of books in the list
     */
    @GetMapping("/{listId}/books")
    public ResponseEntity<List<Book>> getBooksInList(@PathVariable Long listId) {
        return ResponseEntity.ok(bookListService.getBooksInList(listId));
    }

    /**
     * Retrieves all book lists belonging to the current user that do not contain a
     * specific book.
     *
     * @param bookId book identifier
     * @return filtered list of book lists
     * @throws MissingSessionException if the user is not logged in
     */
    @GetMapping("/exclude-book/{bookId}")
    public ResponseEntity<List<BookList>> getListsWithoutBook(@PathVariable Long bookId) {
        Object raw = sessionContext.getUserId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long userId = Long.valueOf(raw.toString());
        return ResponseEntity.ok(bookListService.getListsWithoutBook(userId, bookId));
    }

    /**
     * Generates a share token for a book list.
     *
     * @param listId list identifier
     * @return updated book list with share token
     * @throws MissingSessionException if the user is not logged in
     */
    @PostMapping("/{listId}/share")
    public ResponseEntity<BookList> generateShareToken(@PathVariable Long listId) {
        Object raw = sessionContext.getUserId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long userId = Long.valueOf(raw.toString());
        BookList list = bookListService.generateShareToken(userId, listId);
        return ResponseEntity.ok(list);
    }

    /**
     * Removes the share token from a book list.
     *
     * @param listId list identifier
     * @return updated book list without share token
     * @throws MissingSessionException if the user is not logged in
     */
    @DeleteMapping("/{listId}/share")
    public ResponseEntity<BookList> removeShareToken(@PathVariable Long listId) {
        Object raw = sessionContext.getUserId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long userId = Long.valueOf(raw.toString());
        return ResponseEntity.ok(bookListService.removeShareToken(userId, listId));
    }

    @Data
    public static class CreateListRequest {
        private String name;
    }

    @Data
    public static class AddBookRequest {
        @JsonProperty("bookId")
        private Long bookId;
    }
}
