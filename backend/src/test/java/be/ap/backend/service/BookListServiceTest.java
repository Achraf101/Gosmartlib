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

import be.ap.backend.entity.BookList;
import be.ap.backend.entity.BookListItem;
import be.ap.backend.repository.BookListItemRepository;
import be.ap.backend.repository.BookListRepository;
import be.ap.backend.repository.SavedListRepository;

@SpringBootTest
public class BookListServiceTest {

    @MockitoBean
    private BookListRepository bookListRepository;

    @MockitoBean
    private BookListItemRepository bookListItemRepository;

    @MockitoBean
    private SavedListRepository savedListRepository;

    @Autowired
    private BookListService bookListService;

    @Test
    void givenOwnerIdAndName_whenCreateList_thenReturnSavedList() {
        BookList list = new BookList();
        list.setOwnerId(1L);
        list.setName("My List");
        when(bookListRepository.save(any())).thenReturn(list);

        BookList result = bookListService.createList(1L, "My List");

        assertNotNull(result);
        assertEquals("My List", result.getName());
        verify(bookListRepository, times(1)).save(any());
    }

    @Test
    void givenOwnerId_whenGetListsByOwner_thenReturnLists() {
        BookList list = new BookList();
        list.setId(1L);
        when(bookListRepository.findByOwnerId(1L)).thenReturn(List.of(list));

        List<BookList> result = bookListService.getListsByOwner(1L);

        assertEquals(1, result.size());
        verify(bookListRepository, times(1)).findByOwnerId(1L);
    }

    @Test
    void givenListIdAndName_whenRenameList_thenReturnUpdatedList() {
        BookList list = new BookList();
        list.setId(1L);
        list.setName("Old Name");
        when(bookListRepository.findById(1L)).thenReturn(Optional.of(list));
        when(bookListRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        BookList result = bookListService.renameList(1L, "New Name");

        assertEquals("New Name", result.getName());
        verify(bookListRepository, times(1)).save(any());
    }

    @Test
    void givenInvalidListId_whenRenameList_thenThrowException() {
        when(bookListRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> bookListService.renameList(99L, "Name"));
    }

    @Test
    void givenListId_whenDeleteList_thenCallDeleteById() {
        doNothing().when(bookListRepository).deleteById(1L);

        bookListService.deleteList(1L);

        verify(bookListRepository, times(1)).deleteById(1L);
    }

    @Test
    void givenListIdAndBookId_whenAddBook_thenReturnSavedItem() {
        BookListItem item = new BookListItem();
        item.setBookListId(1L);
        item.setBookId(5L);
        when(bookListItemRepository.save(any())).thenReturn(item);

        BookListItem result = bookListService.addBook(1L, 5L);

        assertNotNull(result);
        assertEquals(5L, result.getBookId());
        verify(bookListItemRepository, times(1)).save(any());
    }

    @Test
    void givenUserIdAndListId_whenGenerateShareToken_thenReturnListWithToken() {
        BookList list = new BookList();
        list.setId(1L);
        list.setOwnerId(1L);
        list.setShareToken(null);
        when(bookListRepository.findById(1L)).thenReturn(Optional.of(list));
        when(bookListRepository.findByShareToken(any())).thenReturn(Optional.empty());
        when(bookListRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        BookList result = bookListService.generateShareToken(1L, 1L);

        assertNotNull(result.getShareToken());
        verify(bookListRepository, times(1)).save(any());
    }

    @Test
    void givenUserIdAndListId_whenGenerateShareToken_alreadyHasToken_thenReturnExistingToken() {
        BookList list = new BookList();
        list.setId(1L);
        list.setOwnerId(1L);
        list.setShareToken("existing");
        when(bookListRepository.findById(1L)).thenReturn(Optional.of(list));

        BookList result = bookListService.generateShareToken(1L, 1L);

        assertEquals("existing", result.getShareToken());
        verify(bookListRepository, never()).save(any());
    }

    @Test
    void givenWrongUserId_whenGenerateShareToken_thenThrowForbidden() {
        BookList list = new BookList();
        list.setId(1L);
        list.setOwnerId(2L);
        when(bookListRepository.findById(1L)).thenReturn(Optional.of(list));

        assertThrows(ResponseStatusException.class, () -> bookListService.generateShareToken(1L, 1L));
    }

    @Test
    void givenUserIdAndListId_whenRemoveShareToken_thenSetTokenToNull() {
        BookList list = new BookList();
        list.setId(1L);
        list.setOwnerId(1L);
        list.setShareToken("abc123");
        when(bookListRepository.findById(1L)).thenReturn(Optional.of(list));
        when(bookListRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doNothing().when(savedListRepository).deleteByBookListId(1L);

        BookList result = bookListService.removeShareToken(1L, 1L);

        assertNull(result.getShareToken());
        verify(savedListRepository, times(1)).deleteByBookListId(1L);
    }

    @Test
    void givenWrongUserId_whenRemoveShareToken_thenThrowForbidden() {
        BookList list = new BookList();
        list.setId(1L);
        list.setOwnerId(2L);
        when(bookListRepository.findById(1L)).thenReturn(Optional.of(list));

        assertThrows(ResponseStatusException.class, () -> bookListService.removeShareToken(1L, 1L));
    }
}