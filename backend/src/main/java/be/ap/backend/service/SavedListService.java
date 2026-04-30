package be.ap.backend.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import be.ap.backend.dto.SharedListResponseDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.BookList;
import be.ap.backend.entity.SavedList;
import be.ap.backend.repository.BookListItemRepository;
import be.ap.backend.repository.BookListRepository;
import be.ap.backend.repository.SavedListRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavedListService {

    private final SavedListRepository savedListRepository;
    private final BookListRepository bookListRepository;
    private final BookListItemRepository bookListItemRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public SavedList saveList(Long userId, Long bookListId) {
        if (savedListRepository.existsByUserIdAndBookListId(userId, bookListId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "List already saved");
        }
        SavedList savedList = new SavedList();
        savedList.setUserId(userId);
        savedList.setBookListId(bookListId);
        return savedListRepository.save(savedList);
    }

    public void unsaveList(Long userId, Long bookListId) {
        savedListRepository.deleteByUserIdAndBookListId(userId, bookListId);
    }

    public boolean isSaved(Long userId, Long bookListId) {
        return savedListRepository.existsByUserIdAndBookListId(userId, bookListId);
    }

    public List<SharedListResponseDTO> getSavedLists(Long userId) {
        return savedListRepository.findByUserId(userId).stream()
            .map(saved -> {
                BookList list = bookListRepository.findById(saved.getBookListId())
                    .orElse(null);
                if (list == null) return null;
                List<Book> books = bookListItemRepository.findByBookListId(list.getId())
                    .stream()
                    .map(item -> entityManager.find(Book.class, item.getBookId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                return new SharedListResponseDTO(list, books);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public BookList getListByToken(String token) {
        return bookListRepository.findByShareToken(token)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "List not found"));
    }
}