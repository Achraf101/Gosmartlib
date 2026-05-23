package be.ap.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.entity.BookType;
import be.ap.backend.service.BookTypeService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("booktype")
@RequiredArgsConstructor
public class BookTypeController {

    private final BookTypeService bookTypeService;

    @GetMapping
    public ResponseEntity<List<BookType>> getAll() {
        return ResponseEntity.ok(bookTypeService.getAll());
    }

    @PostMapping
    public ResponseEntity<BookType> addBookType(@RequestBody BookType bookType) {
        return ResponseEntity.ok(bookTypeService.addBookType(bookType));
    }

    @GetMapping("/search/{query}")
    public ResponseEntity<List<BookType>> searchBookType(@PathVariable String query) {
        return ResponseEntity.ok(bookTypeService.search(query));
    }
}