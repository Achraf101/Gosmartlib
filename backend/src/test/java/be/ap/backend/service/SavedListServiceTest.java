package be.ap.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.ap.backend.dto.SharedListResponseDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.BookList;
import be.ap.backend.entity.BookListItem;
import be.ap.backend.entity.SavedList;
import be.ap.backend.exception.BookAlreadyInLocationException;
import be.ap.backend.repository.BookListItemRepository;
import be.ap.backend.repository.BookListRepository;
import be.ap.backend.repository.SavedListRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class SavedListServiceTest {

    @Mock
    private SavedListRepository savedListRepository;

    @Mock
    private BookListRepository bookListRepository;

    @Mock
    private BookListItemRepository bookListItemRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private SavedListService savedListService;

    // --- saveList ---

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
        assertEquals(1L, result.getBookListId());
        verify(savedListRepository, times(1)).save(any());
    }

    @Test
    void givenAlreadySaved_whenSaveList_thenThrowBookAlreadyInLocationException() {
        when(savedListRepository.existsByUserIdAndBookListId(1L, 1L)).thenReturn(true);

        assertThrows(BookAlreadyInLocationException.class, () -> savedListService.saveList(1L, 1L));
        verify(savedListRepository, never()).save(any());
    }

    // --- unsaveList ---

    @Test
    void givenUserIdAndBookListId_whenUnsaveList_thenCallDelete() {
        doNothing().when(savedListRepository).deleteByUserIdAndBookListId(1L, 1L);

        savedListService.unsaveList(1L, 1L);

        verify(savedListRepository, times(1)).deleteByUserIdAndBookListId(1L, 1L);
    }

    // --- isSaved ---

    @Test
    void givenExistingSavedEntry_whenIsSaved_thenReturnTrue() {
        when(savedListRepository.existsByUserIdAndBookListId(1L, 1L)).thenReturn(true);

        assertTrue(savedListService.isSaved(1L, 1L));
        verify(savedListRepository, times(1)).existsByUserIdAndBookListId(1L, 1L);
    }

    @Test
    void givenNoSavedEntry_whenIsSaved_thenReturnFalse() {
        when(savedListRepository.existsByUserIdAndBookListId(1L, 99L)).thenReturn(false);

        assertFalse(savedListService.isSaved(1L, 99L));
        verify(savedListRepository, times(1)).existsByUserIdAndBookListId(1L, 99L);
    }

    // --- getSavedLists ---

    @Test
    void givenUserIdWithNoItems_whenGetSavedLists_thenReturnEmptyBookList() {
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
        assertTrue(result.get(0).getBooks().isEmpty());
    }

    @Test
    void givenUserIdWithItems_whenGetSavedLists_thenReturnMappedBooksViaEntityManager() {
        SavedList saved = new SavedList();
        saved.setUserId(1L);
        saved.setBookListId(1L);

        BookList list = new BookList();
        list.setId(1L);
        list.setName("List With Books");

        BookListItem item = new BookListItem();
        item.setBookId(42L);
        item.setBookListId(1L);

        Book book = new Book();
        book.setId(42L);

        when(savedListRepository.findByUserId(1L)).thenReturn(List.of(saved));
        when(bookListRepository.findById(1L)).thenReturn(Optional.of(list));
        when(bookListItemRepository.findByBookListId(1L)).thenReturn(List.of(item));
        when(entityManager.find(Book.class, 42L)).thenReturn(book);

        List<SharedListResponseDTO> result = savedListService.getSavedLists(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getBooks().size());
        assertEquals(42L, result.get(0).getBooks().get(0).getId());
    }

    @Test
    void givenItemWithUnresolvableBookId_whenGetSavedLists_thenSkipNullBook() {
        SavedList saved = new SavedList();
        saved.setUserId(1L);
        saved.setBookListId(1L);

        BookList list = new BookList();
        list.setId(1L);
        list.setName("List With Missing Book");

        BookListItem item = new BookListItem();
        item.setBookId(999L);
        item.setBookListId(1L);

        when(savedListRepository.findByUserId(1L)).thenReturn(List.of(saved));
        when(bookListRepository.findById(1L)).thenReturn(Optional.of(list));
        when(bookListItemRepository.findByBookListId(1L)).thenReturn(List.of(item));
        when(entityManager.find(Book.class, 999L)).thenReturn(null);

        List<SharedListResponseDTO> result = savedListService.getSavedLists(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getBooks().isEmpty());
    }

    @Test
    void givenBookListNotFound_whenGetSavedLists_thenSkipEntry() {
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
    void givenNoSavedLists_whenGetSavedLists_thenReturnEmptyList() {
        when(savedListRepository.findByUserId(1L)).thenReturn(List.of());

        List<SharedListResponseDTO> result = savedListService.getSavedLists(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(bookListRepository, bookListItemRepository, entityManager);
    }

    // --- getListByToken ---

    @Test
    void givenValidToken_whenGetListByToken_thenReturnList() {
        BookList list = new BookList();
        list.setId(1L);
        list.setShareToken("abc123");

        when(bookListRepository.findByShareToken("abc123")).thenReturn(Optional.of(list));

        BookList result = savedListService.getListByToken("abc123");

        assertNotNull(result);
        assertEquals("abc123", result.getShareToken());
    }

    @Test
    void givenInvalidToken_whenGetListByToken_thenThrowEntityNotFoundException() {
        when(bookListRepository.findByShareToken("invalid")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> savedListService.getListByToken("invalid"));
    }
}