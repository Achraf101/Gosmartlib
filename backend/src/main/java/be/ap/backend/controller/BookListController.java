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
import be.ap.backend.service.BookListService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import be.ap.backend.dto.SharedListResponseDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("lists")
@RequiredArgsConstructor
public class BookListController {

    private final BookListService bookListService;

    @PersistenceContext
    private EntityManager entityManager;

    @PostMapping("/{userId}")
    public ResponseEntity<BookList> createList(@PathVariable Long userId, @RequestBody CreateListRequest request) {
        BookList list = bookListService.createList(userId, request.getName());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<BookList>> getMyLists(@PathVariable Long userId) {
        return ResponseEntity.ok(bookListService.getListsByOwner(userId));
    }

    @PostMapping("/{userId}/{listId}/books")
    public ResponseEntity<BookListItem> addBook(@PathVariable Long listId, @RequestBody AddBookRequest request) {
        BookListItem book = bookListService.addBook(listId, request.getBookId());
        return ResponseEntity.ok(book);
    }

    @DeleteMapping("/{userId}/{listId}/books/{bookId}")
    public ResponseEntity<Void> removeBook(@PathVariable Long bookId, @PathVariable Long listId) {
        bookListService.removeBook(bookId, listId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/{listId}")
    public ResponseEntity<BookList> renameList(@PathVariable Long listId, @RequestBody CreateListRequest request) {
        BookList list = bookListService.renameList(listId, request.getName());
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{userId}/{listId}")
    public ResponseEntity<Void> deleteList(@PathVariable Long listId) {
        bookListService.deleteList(listId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/shared/{token}")
    public ResponseEntity<SharedListResponseDTO> getSharedList(@PathVariable String token) {
        return ResponseEntity.ok(bookListService.getSharedList(token));
    }

    @GetMapping("/{userId}/{listId}/books")
    public ResponseEntity<List<Book>> getBooksInList(@PathVariable Long listId) {
        return ResponseEntity.ok(bookListService.getBooksInList(listId));
    }

    @GetMapping("/{userId}/exclude-book/{bookId}")
    public ResponseEntity<List<BookList>> getListsWithoutBook(
            @PathVariable Long userId,
            @PathVariable Long bookId) {

        return ResponseEntity.ok(bookListService.getListsWithoutBook(userId, bookId));
    }

    @PostMapping("/{userId}/{listId}/share")
    public ResponseEntity<BookList> generateShareToken(@PathVariable Long userId, @PathVariable Long listId) {
        BookList list = bookListService.generateShareToken(userId, listId);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{userId}/{listId}/share")
    public ResponseEntity<BookList> removeShareToken(@PathVariable Long userId, @PathVariable Long listId) {
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
