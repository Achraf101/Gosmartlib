package be.ap.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.SharedListResponseDTO;
import be.ap.backend.entity.BookList;
import be.ap.backend.entity.SavedList;
import be.ap.backend.exception.MissingSessionException;
import be.ap.backend.service.SavedListService;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing saved book lists for a user.
 * <p>
 * Allows users to save shared book lists via token, retrieve saved lists,
 * check existence, and remove saved lists.
 * </p>
 */
@RestController
@RequestMapping("saved-lists")
@RequiredArgsConstructor
public class SavedListController {

    private final SavedListService savedListService;
    private final SessionContext sessionContext;

    /**
     * Retrieves all saved book lists for the currently authenticated user.
     *
     * @return list of saved book lists
     */
    @GetMapping
    public ResponseEntity<List<SharedListResponseDTO>> getSavedLists() {
        Object raw = sessionContext.getUserId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long userId = Long.valueOf(raw.toString());
        return ResponseEntity.ok(savedListService.getSavedLists(userId));
    }

    /**
     * Saves a shared book list for the current user using a share token.
     *
     * @param token the share token of the book list
     * @return the created saved list entry
     */
    @PostMapping("/{token}")
    public ResponseEntity<SavedList> saveList(@PathVariable String token) {
        Object raw = sessionContext.getUserId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long userId = Long.valueOf(raw.toString());
        BookList list = savedListService.getListByToken(token);
        SavedList saved = savedListService.saveList(userId, list.getId());
        return ResponseEntity.ok(saved);
    }

    /**
     * Removes a saved book list for the current user.
     *
     * @param bookListId the ID of the book list to remove
     * @return empty response on success
     */
    @DeleteMapping("/{bookListId}")
    public ResponseEntity<Void> unsaveList(@PathVariable Long bookListId) {
        Object raw = sessionContext.getUserId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long userId = Long.valueOf(raw.toString());
        savedListService.unsaveList(userId, bookListId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Checks whether the current user has saved a specific book list.
     *
     * @param bookListId the ID of the book list
     * @return true if saved, false otherwise
     */
    @GetMapping("/{bookListId}/exists")
    public ResponseEntity<Boolean> isSaved(@PathVariable Long bookListId) {
        Object raw = sessionContext.getUserId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long userId = Long.valueOf(raw.toString());
        return ResponseEntity.ok(savedListService.isSaved(userId, bookListId));
    }
}
