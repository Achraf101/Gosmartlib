package be.ap.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.dto.SharedListResponseDTO;
import be.ap.backend.entity.BookList;
import be.ap.backend.entity.SavedList;
import be.ap.backend.exception.MissingSessionException;
import be.ap.backend.service.SavedListService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("saved-lists")
@RequiredArgsConstructor
public class SavedListController {

    private final SavedListService savedListService;

    @GetMapping
    public ResponseEntity<List<SharedListResponseDTO>> getSavedLists(HttpSession session) {
        Object raw = session.getAttribute("userId");
        if (raw == null) throw new MissingSessionException("Niet ingelogd");
        Long userId = Long.valueOf(raw.toString());
        return ResponseEntity.ok(savedListService.getSavedLists(userId));
    }

    @PostMapping("/{token}")
    public ResponseEntity<SavedList> saveList(HttpSession session, @PathVariable String token) {
        Object raw = session.getAttribute("userId");
        if (raw == null) throw new MissingSessionException("Niet ingelogd");
        Long userId = Long.valueOf(raw.toString());
        BookList list = savedListService.getListByToken(token);
        SavedList saved = savedListService.saveList(userId, list.getId());
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{bookListId}")
    public ResponseEntity<Void> unsaveList(HttpSession session, @PathVariable Long bookListId) {
        Object raw = session.getAttribute("userId");
        if (raw == null) throw new MissingSessionException("Niet ingelogd");
        Long userId = Long.valueOf(raw.toString());
        savedListService.unsaveList(userId, bookListId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{bookListId}/exists")
    public ResponseEntity<Boolean> isSaved(HttpSession session, @PathVariable Long bookListId) {
        Object raw = session.getAttribute("userId");
        if (raw == null) throw new MissingSessionException("Niet ingelogd");
        Long userId = Long.valueOf(raw.toString());
        return ResponseEntity.ok(savedListService.isSaved(userId, bookListId));
    }
}
