package be.ap.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import be.ap.backend.dto.SharedListResponseDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.BookList;
import be.ap.backend.entity.BookListItem;
import be.ap.backend.repository.BookListItemRepository;
import be.ap.backend.repository.BookListRepository;
import be.ap.backend.repository.SavedListRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class BookListServiceTest {

    @Mock
    private BookListRepository bookListRepository;

    @Mock
    private BookListItemRepository bookListItemRepository;

    @Mock
    private SavedListRepository savedListRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private BookListService bookListService;

    @BeforeEach
    void injectEntityManager() {
        // @PersistenceContext is not handled by Mockito's @InjectMocks,
        // so we inject the mock manually after construction.
        ReflectionTestUtils.setField(bookListService, "entityManager", entityManager);
    }

    // -------------------------------------------------------------------------
    // createList
    // -------------------------------------------------------------------------

    @Test
    void givenOwnerIdAndName_whenCreateList_thenReturnSavedList() {
        BookList list = new BookList();
        list.setOwnerId(1L);
        list.setName("My List");
        when(bookListRepository.save(any())).thenReturn(list);

        BookList result = bookListService.createList(1L, "My List");

        assertNotNull(result);
        assertEquals("My List", result.getName());
        assertEquals(1L, result.getOwnerId());
        verify(bookListRepository, times(1)).save(any());
    }

    @Test
    void givenOwnerIdAndName_whenCreateList_thenShareTokenIsNull() {
        BookList list = new BookList();
        list.setOwnerId(1L);
        list.setName("My List");
        list.setShareToken(null);
        when(bookListRepository.save(any())).thenReturn(list);

        BookList result = bookListService.createList(1L, "My List");

        assertNull(result.getShareToken());
    }

    // -------------------------------------------------------------------------
    // getListsByOwner
    // -------------------------------------------------------------------------

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
    void givenOwnerIdWithNoLists_whenGetListsByOwner_thenReturnEmptyList() {
        when(bookListRepository.findByOwnerId(99L)).thenReturn(List.of());

        List<BookList> result = bookListService.getListsByOwner(99L);

        assertTrue(result.isEmpty());
    }

    // -------------------------------------------------------------------------
    // getListById
    // -------------------------------------------------------------------------

    @Test
    void givenValidListId_whenGetListById_thenReturnList() {
        BookList list = new BookList();
        list.setId(1L);
        when(bookListRepository.findById(1L)).thenReturn(Optional.of(list));

        Optional<BookList> result = bookListService.getListById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void givenInvalidListId_whenGetListById_thenReturnEmpty() {
        when(bookListRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<BookList> result = bookListService.getListById(99L);

        assertTrue(result.isEmpty());
    }

    // -------------------------------------------------------------------------
    // getListByToken
    // -------------------------------------------------------------------------

    @Test
    void givenValidToken_whenGetListByToken_thenReturnList() {
        BookList list = new BookList();
        list.setShareToken("abc123");
        when(bookListRepository.findByShareToken("abc123")).thenReturn(Optional.of(list));

        Optional<BookList> result = bookListService.getListByToken("abc123");

        assertTrue(result.isPresent());
        assertEquals("abc123", result.get().getShareToken());
    }

    @Test
    void givenUnknownToken_whenGetListByToken_thenReturnEmpty() {
        when(bookListRepository.findByShareToken("unknown")).thenReturn(Optional.empty());

        Optional<BookList> result = bookListService.getListByToken("unknown");

        assertTrue(result.isEmpty());
    }

    // -------------------------------------------------------------------------
    // renameList
    // -------------------------------------------------------------------------

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
    void givenInvalidListId_whenRenameList_thenThrowEntityNotFoundException() {
        when(bookListRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookListService.renameList(99L, "Name"));
    }

    // -------------------------------------------------------------------------
    // deleteList
    // -------------------------------------------------------------------------

    @Test
    void givenListId_whenDeleteList_thenCallDeleteById() {
        doNothing().when(bookListRepository).deleteById(1L);

        bookListService.deleteList(1L);

        verify(bookListRepository, times(1)).deleteById(1L);
    }

    // -------------------------------------------------------------------------
    // addBook
    // -------------------------------------------------------------------------

    @Test
    void givenListIdAndBookId_whenAddBook_thenReturnSavedItem() {
        BookListItem item = new BookListItem();
        item.setBookListId(1L);
        item.setBookId(5L);
        when(bookListItemRepository.save(any())).thenReturn(item);

        BookListItem result = bookListService.addBook(1L, 5L);

        assertNotNull(result);
        assertEquals(5L, result.getBookId());
        assertEquals(1L, result.getBookListId());
        verify(bookListItemRepository, times(1)).save(any());
    }

    // -------------------------------------------------------------------------
    // removeBook
    // -------------------------------------------------------------------------

    @Test
    void givenBookIdAndListId_whenRemoveBook_thenCallDeleteByBookIdAndBookListId() {
        doNothing().when(bookListItemRepository).deleteByBookIdAndBookListId(5L, 1L);

        bookListService.removeBook(5L, 1L);

        verify(bookListItemRepository, times(1)).deleteByBookIdAndBookListId(5L, 1L);
    }

    // -------------------------------------------------------------------------
    // getBooksInList
    // -------------------------------------------------------------------------

    @Test
    void givenListIdWithBooks_whenGetBooksInList_thenReturnBooks() {
        BookListItem item = new BookListItem();
        item.setBookId(10L);
        item.setBookListId(1L);

        Book book = new Book();
        book.setId(10L);

        when(bookListItemRepository.findByBookListId(1L)).thenReturn(List.of(item));
        when(entityManager.find(Book.class, 10L)).thenReturn(book);

        List<Book> result = bookListService.getBooksInList(1L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    void givenListIdWithMissingBook_whenGetBooksInList_thenFilterOutNulls() {
        BookListItem item = new BookListItem();
        item.setBookId(99L);
        item.setBookListId(1L);

        when(bookListItemRepository.findByBookListId(1L)).thenReturn(List.of(item));
        when(entityManager.find(Book.class, 99L)).thenReturn(null);

        List<Book> result = bookListService.getBooksInList(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void givenListIdWithNoItems_whenGetBooksInList_thenReturnEmptyList() {
        when(bookListItemRepository.findByBookListId(1L)).thenReturn(List.of());

        List<Book> result = bookListService.getBooksInList(1L);

        assertTrue(result.isEmpty());
        verify(entityManager, never()).find(any(), any());
    }

    // -------------------------------------------------------------------------
    // getListsWithoutBook
    // -------------------------------------------------------------------------

    @Test
    void givenUserIdAndBookId_whenGetListsWithoutBook_thenReturnFilteredLists() {
        BookList list = new BookList();
        list.setId(2L);
        when(bookListRepository.findListsWithoutBook(1L, 5L)).thenReturn(List.of(list));

        List<BookList> result = bookListService.getListsWithoutBook(1L, 5L);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
        verify(bookListRepository, times(1)).findListsWithoutBook(1L, 5L);
    }

    // -------------------------------------------------------------------------
    // generateShareToken
    // -------------------------------------------------------------------------

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
        assertFalse(result.getShareToken().isBlank());
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

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> bookListService.generateShareToken(1L, 1L));

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void givenInvalidListId_whenGenerateShareToken_thenThrowNotFound() {
        when(bookListRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> bookListService.generateShareToken(1L, 99L));

        assertEquals(404, ex.getStatusCode().value());
    }

    // -------------------------------------------------------------------------
    // removeShareToken
    // -------------------------------------------------------------------------

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
        verify(bookListRepository, times(1)).save(any());
    }

    @Test
    void givenWrongUserId_whenRemoveShareToken_thenThrowForbidden() {
        BookList list = new BookList();
        list.setId(1L);
        list.setOwnerId(2L);
        when(bookListRepository.findById(1L)).thenReturn(Optional.of(list));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> bookListService.removeShareToken(1L, 1L));

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void givenInvalidListId_whenRemoveShareToken_thenThrowNotFound() {
        when(bookListRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> bookListService.removeShareToken(1L, 99L));

        assertEquals(404, ex.getStatusCode().value());
    }

    // -------------------------------------------------------------------------
    // getSharedList
    // -------------------------------------------------------------------------

    @Test
    void givenValidToken_whenGetSharedList_thenReturnDTO() {
        BookList list = new BookList();
        list.setId(1L);
        list.setShareToken("abc123");

        BookListItem item = new BookListItem();
        item.setBookId(10L);
        item.setBookListId(1L);

        Book book = new Book();
        book.setId(10L);

        when(bookListRepository.findByShareToken("abc123")).thenReturn(Optional.of(list));
        when(bookListItemRepository.findByBookListId(1L)).thenReturn(List.of(item));
        when(entityManager.find(Book.class, 10L)).thenReturn(book);

        SharedListResponseDTO result = bookListService.getSharedList("abc123");

        assertNotNull(result);
        assertEquals(list, result.getList());
        assertEquals(1, result.getBooks().size());
        assertEquals(10L, result.getBooks().get(0).getId());
    }

    @Test
    void givenInvalidToken_whenGetSharedList_thenThrowNotFound() {
        when(bookListRepository.findByShareToken("bad")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> bookListService.getSharedList("bad"));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void givenValidTokenWithMissingBooks_whenGetSharedList_thenReturnDTOWithEmptyBookList() {
        BookList list = new BookList();
        list.setId(1L);
        list.setShareToken("abc123");

        BookListItem item = new BookListItem();
        item.setBookId(99L);
        item.setBookListId(1L);

        when(bookListRepository.findByShareToken("abc123")).thenReturn(Optional.of(list));
        when(bookListItemRepository.findByBookListId(1L)).thenReturn(List.of(item));
        when(entityManager.find(Book.class, 99L)).thenReturn(null);

        SharedListResponseDTO result = bookListService.getSharedList("abc123");

        assertNotNull(result);
        assertTrue(result.getBooks().isEmpty());
    }
}