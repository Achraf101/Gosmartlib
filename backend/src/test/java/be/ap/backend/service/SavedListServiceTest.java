package be.ap.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;

import be.ap.backend.dto.SharedListResponseDTO;
import be.ap.backend.entity.BookList;
import be.ap.backend.entity.SavedList;
import be.ap.backend.repository.BookListItemRepository;
import be.ap.backend.repository.BookListRepository;
import be.ap.backend.repository.SavedListRepository;

@SpringBootTest
public class SavedListServiceTest {

    @MockitoBean
    private SavedListRepository savedListRepository;

    @MockitoBean
    private BookListRepository bookListRepository;

    @MockitoBean
    private BookListItemRepository bookListItemRepository;

    @Autowired
    private SavedListService savedListService;

    @Test
    void givenUserIdAndBookListId_whenSaveList_thenReturnSavedList() {
        when(savedListRepository.existsByUserIdAndBookListId(1L, 1L)).thenReturn(false);
        SavedList savedList = new SavedList();
        savedList.setUserId(1L);
        savedList.setBookListId(1L);
        when(savedListRepository.save(any())).thenReturn(savedList);

        SavedList result = savedListService.saveList(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        verify(savedListRepository, times(1)).save(any());
    }

    @Test
    void givenAlreadySaved_whenSaveList_thenThrowConflict() {
        when(savedListRepository.existsByUserIdAndBookListId(1L, 1L)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> savedListService.saveList(1L, 1L));
        verify(savedListRepository, never()).save(any());
    }

    @Test
    void givenUserIdAndBookListId_whenUnsaveList_thenCallDelete() {
        doNothing().when(savedListRepository).deleteByUserIdAndBookListId(1L, 1L);

        savedListService.unsaveList(1L, 1L);

        verify(savedListRepository, times(1)).deleteByUserIdAndBookListId(1L, 1L);
    }

    @Test
    void givenUserIdAndBookListId_whenIsSaved_thenReturnTrue() {
        when(savedListRepository.existsByUserIdAndBookListId(1L, 1L)).thenReturn(true);

        boolean result = savedListService.isSaved(1L, 1L);

        assertTrue(result);
        verify(savedListRepository, times(1)).existsByUserIdAndBookListId(1L, 1L);
    }

    @Test
    void givenUserIdAndBookListId_whenIsSaved_thenReturnFalse_whenNotSaved() {
        when(savedListRepository.existsByUserIdAndBookListId(1L, 99L)).thenReturn(false);

        boolean result = savedListService.isSaved(1L, 99L);

        assertFalse(result);
    }

    @Test
    void givenUserId_whenGetSavedLists_thenReturnMappedResponses() {
        SavedList saved = new SavedList();
        saved.setUserId(1L);
        saved.setBookListId(1L);
        BookList list = new BookList();
        list.setId(1L);
        list.setName("Shared List");

        when(savedListRepository.findByUserId(1L)).thenReturn(List.of(saved));
        when(bookListRepository.findById(1L)).thenReturn(Optional.of(list));
        when(bookListItemRepository.findByBookListId(1L)).thenReturn(List.of());

        List<SharedListResponseDTO> result = savedListService.getSavedLists(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Shared List", result.get(0).getList().getName());
    }

    @Test
    void givenUserId_whenGetSavedLists_thenSkipNullLists() {
        SavedList saved = new SavedList();
        saved.setUserId(1L);
        saved.setBookListId(99L);

        when(savedListRepository.findByUserId(1L)).thenReturn(List.of(saved));
        when(bookListRepository.findById(99L)).thenReturn(Optional.empty());

        List<SharedListResponseDTO> result = savedListService.getSavedLists(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenToken_whenGetListByToken_thenReturnList() {
        BookList list = new BookList();
        list.setId(1L);
        list.setShareToken("abc123");
        when(bookListRepository.findByShareToken("abc123")).thenReturn(Optional.of(list));

        BookList result = savedListService.getListByToken("abc123");

        assertNotNull(result);
        assertEquals("abc123", result.getShareToken());
    }

    @Test
    void givenInvalidToken_whenGetListByToken_thenThrowNotFound() {
        when(bookListRepository.findByShareToken("invalid")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> savedListService.getListByToken("invalid"));
    }
}