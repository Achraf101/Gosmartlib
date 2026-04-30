package be.ap.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.entity.BookList;
import be.ap.backend.entity.SavedList;
import be.ap.backend.service.SavedListService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("saved-lists")
@RequiredArgsConstructor
public class SavedListController {

    private final SavedListService savedListService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<be.ap.backend.dto.SharedListResponseDTO>> getSavedLists(@PathVariable Long userId) {
        return ResponseEntity.ok(savedListService.getSavedLists(userId));
    }

    @PostMapping("/{userId}/{token}")
    public ResponseEntity<SavedList> saveList(@PathVariable Long userId, @PathVariable String token) {
        BookList list = savedListService.getListByToken(token);
        SavedList saved = savedListService.saveList(userId, list.getId());
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{userId}/{bookListId}")
    public ResponseEntity<Void> unsaveList(@PathVariable Long userId, @PathVariable Long bookListId) {
        savedListService.unsaveList(userId, bookListId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/{bookListId}/exists")
    public ResponseEntity<Boolean> isSaved(@PathVariable Long userId, @PathVariable Long bookListId) {
        return ResponseEntity.ok(savedListService.isSaved(userId, bookListId));
    }
}
