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
import jakarta.servlet.http.HttpSession;
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

    @PostMapping
    public ResponseEntity<BookList> createList(HttpSession session, @RequestBody CreateListRequest request) {
        Long userId = Long.valueOf(session.getAttribute("userId").toString());
        BookList list = bookListService.createList(userId, request.getName());
        return ResponseEntity.ok(list);
    }

    @GetMapping
    public ResponseEntity<List<BookList>> getMyLists(HttpSession session) {
        Long userId = Long.valueOf(session.getAttribute("userId").toString());
        return ResponseEntity.ok(bookListService.getListsByOwner(userId));
    }

    @PostMapping("/{listId}/books")
    public ResponseEntity<BookListItem> addBook(HttpSession session, @PathVariable Long listId, @RequestBody AddBookRequest request) {
        BookListItem book = bookListService.addBook(listId, request.getBookId());
        return ResponseEntity.ok(book);
    }

    @DeleteMapping("/{listId}/books/{bookId}")
    public ResponseEntity<Void> removeBook(HttpSession session, @PathVariable Long bookId, @PathVariable Long listId) {
        bookListService.removeBook(bookId, listId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{listId}")
    public ResponseEntity<BookList> renameList(HttpSession session, @PathVariable Long listId, @RequestBody CreateListRequest request) {
        BookList list = bookListService.renameList(listId, request.getName());
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> deleteList(HttpSession session, @PathVariable Long listId) {
        bookListService.deleteList(listId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/shared/{token}")
    public ResponseEntity<SharedListResponseDTO> getSharedList(@PathVariable String token) {
        return ResponseEntity.ok(bookListService.getSharedList(token));
    }

    @GetMapping("/{listId}/books")
    public ResponseEntity<List<Book>> getBooksInList(@PathVariable Long listId) {
        return ResponseEntity.ok(bookListService.getBooksInList(listId));
    }

    @GetMapping("/exclude-book/{bookId}")
    public ResponseEntity<List<BookList>> getListsWithoutBook(HttpSession session, @PathVariable Long bookId) {
        Long userId = Long.valueOf(session.getAttribute("userId").toString());
        return ResponseEntity.ok(bookListService.getListsWithoutBook(userId, bookId));
    }

    @PostMapping("/{listId}/share")
    public ResponseEntity<BookList> generateShareToken(HttpSession session, @PathVariable Long listId) {
        Long userId = Long.valueOf(session.getAttribute("userId").toString());
        BookList list = bookListService.generateShareToken(userId, listId);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{listId}/share")
    public ResponseEntity<BookList> removeShareToken(HttpSession session, @PathVariable Long listId) {
        Long userId = Long.valueOf(session.getAttribute("userId").toString());
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
