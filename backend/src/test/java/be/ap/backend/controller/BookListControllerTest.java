package be.ap.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import be.ap.backend.dto.SharedListResponseDTO;
import be.ap.backend.entity.BookList;
import be.ap.backend.entity.BookListItem;
import be.ap.backend.service.BookListService;

@SpringBootTest
public class BookListControllerTest {

    @MockitoBean
    private BookListService bookListService;

    @Autowired
    private BookListController controller;

    @Test
    void givenUserIdAndName_whenCreateList_thenReturnCreatedList() {
        BookList list = new BookList();
        list.setId(1L);
        list.setName("My List");
        when(bookListService.createList(1L, "My List")).thenReturn(list);

        BookListController.CreateListRequest request = new BookListController.CreateListRequest();
        request.setName("My List");

        ResponseEntity<BookList> result = controller.createList(1L, request);

        assertNotNull(result.getBody());
        assertEquals("My List", result.getBody().getName());
        verify(bookListService, times(1)).createList(1L, "My List");
    }

    @Test
    void givenUserId_whenGetMyLists_thenReturnLists() {
        BookList list = new BookList();
        list.setId(1L);
        list.setName("My List");
        when(bookListService.getListsByOwner(1L)).thenReturn(List.of(list));

        ResponseEntity<List<BookList>> result = controller.getMyLists(1L);

        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        verify(bookListService, times(1)).getListsByOwner(1L);
    }

    @Test
    void givenUserId_whenGetMyLists_thenReturnEmpty_whenNoLists() {
        when(bookListService.getListsByOwner(99L)).thenReturn(List.of());

        ResponseEntity<List<BookList>> result = controller.getMyLists(99L);

        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
        verify(bookListService, times(1)).getListsByOwner(99L);
    }

    @Test
    void givenListIdAndBookId_whenAddBook_thenReturnBookListItem() {
        BookListItem item = new BookListItem();
        item.setBookListId(1L);
        item.setBookId(5L);
        when(bookListService.addBook(1L, 5L)).thenReturn(item);

        BookListController.AddBookRequest request = new BookListController.AddBookRequest();
        request.setBookId(5L);

        ResponseEntity<BookListItem> result = controller.addBook(1L, request);

        assertNotNull(result.getBody());
        assertEquals(5L, result.getBody().getBookId());
        verify(bookListService, times(1)).addBook(1L, 5L);
    }

    @Test
    void givenListIdAndBookId_whenRemoveBook_thenReturn204() {
        doNothing().when(bookListService).removeBook(5L, 1L);

        ResponseEntity<Void> result = controller.removeBook(5L, 1L);

        assertEquals(204, result.getStatusCode().value());
        verify(bookListService, times(1)).removeBook(5L, 1L);
    }

    @Test
    void givenListIdAndName_whenRenameList_thenReturnUpdatedList() {
        BookList list = new BookList();
        list.setId(1L);
        list.setName("New Name");
        when(bookListService.renameList(1L, "New Name")).thenReturn(list);

        BookListController.CreateListRequest request = new BookListController.CreateListRequest();
        request.setName("New Name");

        ResponseEntity<BookList> result = controller.renameList(1L, request);

        assertNotNull(result.getBody());
        assertEquals("New Name", result.getBody().getName());
        verify(bookListService, times(1)).renameList(1L, "New Name");
    }

    @Test
    void givenListId_whenDeleteList_thenReturn204() {
        doNothing().when(bookListService).deleteList(1L);

        ResponseEntity<Void> result = controller.deleteList(1L);

        assertEquals(204, result.getStatusCode().value());
        verify(bookListService, times(1)).deleteList(1L);
    }

    @Test
    void givenToken_whenGetSharedList_thenReturnSharedListResponse() {
        BookList list = new BookList();
        list.setId(1L);
        list.setName("Shared List");
        SharedListResponseDTO response = new SharedListResponseDTO(list, List.of());
        when(bookListService.getSharedList("abc123")).thenReturn(response);

        ResponseEntity<SharedListResponseDTO> result = controller.getSharedList("abc123");

        assertNotNull(result.getBody());
        assertEquals("Shared List", result.getBody().getList().getName());
        verify(bookListService, times(1)).getSharedList("abc123");
    }

    @Test
    void givenUserIdAndListId_whenGenerateShareToken_thenReturnListWithToken() {
        BookList list = new BookList();
        list.setId(1L);
        list.setShareToken("abc123");
        when(bookListService.generateShareToken(1L, 1L)).thenReturn(list);

        ResponseEntity<BookList> result = controller.generateShareToken(1L, 1L);

        assertNotNull(result.getBody());
        assertEquals("abc123", result.getBody().getShareToken());
        verify(bookListService, times(1)).generateShareToken(1L, 1L);
    }

    @Test
    void givenUserIdAndListId_whenRemoveShareToken_thenReturnListWithNullToken() {
        BookList list = new BookList();
        list.setId(1L);
        list.setShareToken(null);
        when(bookListService.removeShareToken(1L, 1L)).thenReturn(list);

        ResponseEntity<BookList> result = controller.removeShareToken(1L, 1L);

        assertNotNull(result.getBody());
        assertNull(result.getBody().getShareToken());
        verify(bookListService, times(1)).removeShareToken(1L, 1L);
    }
}