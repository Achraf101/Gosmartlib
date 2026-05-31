package be.ap.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.SharedListResponseDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.BookList;
import be.ap.backend.entity.BookListItem;
import be.ap.backend.exception.MissingSessionException;
import be.ap.backend.service.BookListService;

@ExtendWith(MockitoExtension.class)
public class BookListControllerTest {

    @Mock
    private BookListService bookListService;

    @Mock
    private SessionContext sessionContext;

    @InjectMocks
    private BookListController controller;

    // -------------------------------------------------------------------------
    // createList
    // -------------------------------------------------------------------------

    @Test
    void givenAuthenticatedUser_whenCreateList_thenReturnCreatedList() {
        when(sessionContext.getUserId()).thenReturn(1L);

        BookList list = new BookList();
        list.setId(1L);
        list.setName("My List");
        when(bookListService.createList(1L, "My List")).thenReturn(list);

        BookListController.CreateListRequest request = new BookListController.CreateListRequest();
        request.setName("My List");

        ResponseEntity<BookList> result = controller.createList(request);

        assertNotNull(result.getBody());
        assertEquals("My List", result.getBody().getName());
        verify(bookListService).createList(1L, "My List");
    }

    @Test
    void givenNoSession_whenCreateList_thenThrowMissingSessionException() {
        when(sessionContext.getUserId()).thenReturn(null);

        BookListController.CreateListRequest request = new BookListController.CreateListRequest();
        request.setName("My List");

        assertThrows(MissingSessionException.class, () -> controller.createList(request));
        verifyNoInteractions(bookListService);
    }

    // -------------------------------------------------------------------------
    // getMyLists
    // -------------------------------------------------------------------------

    @Test
    void givenAuthenticatedUser_whenGetMyLists_thenReturnLists() {
        when(sessionContext.getUserId()).thenReturn(1L);

        BookList list = new BookList();
        list.setId(1L);
        when(bookListService.getListsByOwner(1L)).thenReturn(List.of(list));

        ResponseEntity<List<BookList>> result = controller.getMyLists();

        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        verify(bookListService).getListsByOwner(1L);
    }

    @Test
    void givenAuthenticatedUser_whenGetMyLists_thenReturnEmptyList_whenNoLists() {
        when(sessionContext.getUserId()).thenReturn(1L);
        when(bookListService.getListsByOwner(1L)).thenReturn(List.of());

        ResponseEntity<List<BookList>> result = controller.getMyLists();

        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    void givenNoSession_whenGetMyLists_thenThrowMissingSessionException() {
        when(sessionContext.getUserId()).thenReturn(null);

        assertThrows(MissingSessionException.class, () -> controller.getMyLists());
        verifyNoInteractions(bookListService);
    }

    // -------------------------------------------------------------------------
    // addBook
    // -------------------------------------------------------------------------

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
        verify(bookListService).addBook(1L, 5L);
    }

    // -------------------------------------------------------------------------
    // removeBook
    // -------------------------------------------------------------------------

    @Test
    void givenListIdAndBookId_whenRemoveBook_thenReturn204() {
        doNothing().when(bookListService).removeBook(5L, 1L);

        ResponseEntity<Void> result = controller.removeBook(5L, 1L);

        assertEquals(204, result.getStatusCode().value());
        verify(bookListService).removeBook(5L, 1L);
    }

    // -------------------------------------------------------------------------
    // renameList
    // -------------------------------------------------------------------------

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
        verify(bookListService).renameList(1L, "New Name");
    }

    // -------------------------------------------------------------------------
    // deleteList
    // -------------------------------------------------------------------------

    @Test
    void givenListId_whenDeleteList_thenReturn204() {
        doNothing().when(bookListService).deleteList(1L);

        ResponseEntity<Void> result = controller.deleteList(1L);

        assertEquals(204, result.getStatusCode().value());
        verify(bookListService).deleteList(1L);
    }

    // -------------------------------------------------------------------------
    // getSharedList
    // -------------------------------------------------------------------------

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
        verify(bookListService).getSharedList("abc123");
    }

    // -------------------------------------------------------------------------
    // getBooksInList
    // -------------------------------------------------------------------------

    @Test
    void givenListId_whenGetBooksInList_thenReturnBooks() {
        Book book = new Book();
        book.setId(5L);
        when(bookListService.getBooksInList(1L)).thenReturn(List.of(book));

        ResponseEntity<List<Book>> result = controller.getBooksInList(1L);

        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals(5L, result.getBody().get(0).getId());
        verify(bookListService).getBooksInList(1L);
    }

    @Test
    void givenListIdWithNoBooks_whenGetBooksInList_thenReturnEmptyList() {
        when(bookListService.getBooksInList(1L)).thenReturn(List.of());

        ResponseEntity<List<Book>> result = controller.getBooksInList(1L);

        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
    }

    // -------------------------------------------------------------------------
    // getListsWithoutBook
    // -------------------------------------------------------------------------

    @Test
    void givenAuthenticatedUserAndBookId_whenGetListsWithoutBook_thenReturnLists() {
        when(sessionContext.getUserId()).thenReturn(1L);

        BookList list = new BookList();
        list.setId(1L);
        when(bookListService.getListsWithoutBook(1L, 5L)).thenReturn(List.of(list));

        ResponseEntity<List<BookList>> result = controller.getListsWithoutBook(5L);

        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        verify(bookListService).getListsWithoutBook(1L, 5L);
    }

    @Test
    void givenNoSession_whenGetListsWithoutBook_thenThrowMissingSessionException() {
        when(sessionContext.getUserId()).thenReturn(null);

        assertThrows(MissingSessionException.class, () -> controller.getListsWithoutBook(5L));
        verifyNoInteractions(bookListService);
    }

    // -------------------------------------------------------------------------
    // generateShareToken
    // -------------------------------------------------------------------------

    @Test
    void givenAuthenticatedUserAndListId_whenGenerateShareToken_thenReturnListWithToken() {
        when(sessionContext.getUserId()).thenReturn(1L);

        BookList list = new BookList();
        list.setId(1L);
        list.setShareToken("abc123");
        when(bookListService.generateShareToken(1L, 1L)).thenReturn(list);

        ResponseEntity<BookList> result = controller.generateShareToken(1L);

        assertNotNull(result.getBody());
        assertEquals("abc123", result.getBody().getShareToken());
        verify(bookListService).generateShareToken(1L, 1L);
    }

    @Test
    void givenNoSession_whenGenerateShareToken_thenThrowMissingSessionException() {
        when(sessionContext.getUserId()).thenReturn(null);

        assertThrows(MissingSessionException.class, () -> controller.generateShareToken(1L));
        verifyNoInteractions(bookListService);
    }

    // -------------------------------------------------------------------------
    // removeShareToken
    // -------------------------------------------------------------------------

    @Test
    void givenAuthenticatedUserAndListId_whenRemoveShareToken_thenReturnListWithNullToken() {
        when(sessionContext.getUserId()).thenReturn(1L);

        BookList list = new BookList();
        list.setId(1L);
        list.setShareToken(null);
        when(bookListService.removeShareToken(1L, 1L)).thenReturn(list);

        ResponseEntity<BookList> result = controller.removeShareToken(1L);

        assertNotNull(result.getBody());
        assertNull(result.getBody().getShareToken());
        verify(bookListService).removeShareToken(1L, 1L);
    }

    @Test
    void givenNoSession_whenRemoveShareToken_thenThrowMissingSessionException() {
        when(sessionContext.getUserId()).thenReturn(null);

        assertThrows(MissingSessionException.class, () -> controller.removeShareToken(1L));
        verifyNoInteractions(bookListService);
    }
}