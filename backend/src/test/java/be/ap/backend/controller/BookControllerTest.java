package be.ap.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import be.ap.backend.dto.BookCardDTO;
import be.ap.backend.dto.BookLookupDTO;
import be.ap.backend.dto.BookResultDTO;
import be.ap.backend.dto.CreateBookDTO;
import be.ap.backend.entity.Author;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Clib;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.service.BookService;
import be.ap.backend.service.IsbnLookupService;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
public class BookControllerTest {

    @Mock
    private BookService service;

    @Mock
    private BookRepository repository;

    @Mock
    private IsbnLookupService isbnLookupService;

    @InjectMocks
    private BookController controller;

    @Mock
    HttpSession session;

    @BeforeEach
    void setUp() {
        session = mock(HttpSession.class);
        lenient().when(session.getAttribute("campus")).thenReturn("[1, 2]");
    }

    @Test
    void givenBook_whenAddBook_thenReturnSavedBook() {
        CreateBookDTO inputBook = new CreateBookDTO();
        inputBook.setTitle("Test book");

        Book savedBook = new Book();
        savedBook.setId(1L);
        savedBook.setTitle("Test book");

        when(service.saveBook(inputBook)).thenReturn(savedBook);

        Book result = controller.addBook(inputBook);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test book", result.getTitle());
        verify(service, times(1)).saveBook(inputBook);
    }

    @Test
    void givenBooksExist_whenGetAll_thenReturnPagedBooks() {
        Book book1 = new Book();
        book1.setId(1L);
        book1.setTitle("Book 1");

        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Book 2");

        Page<Book> page = new PageImpl<>(List.of(book1, book2));
        when(repository.findAllByCampus(any(), any(Pageable.class))).thenReturn(page);

        Page<Book> result = controller.getAll(session, false, null, 0, 5);

        assertEquals(2, result.getContent().size());
        assertEquals("Book 1", result.getContent().get(0).getTitle());
        assertEquals("Book 2", result.getContent().get(1).getTitle());
        verify(repository, times(1)).findAllByCampus(any(), any(Pageable.class));
    }

    @Test
    void givenExistingId_whenGetById_thenReturnBook() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");

        when(repository.findById(1L)).thenReturn(Optional.of(book));

        Book result = controller.getById(1L);

        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void givenMissingId_whenGetById_thenReturnNull() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        Book result = controller.getById(99L);

        assertEquals(null, result);
        verify(repository, times(1)).findById(99L);
    }

    @Test
    void givenSearchQuery_whenSearch_thenReturnMatchingBooks() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Harry Potter");
        Page<Book> page = new PageImpl<>(List.of(book));

        when(repository.search(any(), eq("Harry"), any(Pageable.class))).thenReturn(page);

        Page<Book> result = controller.search(session, "Harry", 0, 5);

        assertEquals(1, result.getContent().size());
        assertEquals("Harry Potter", result.getContent().get(0).getTitle());
        verify(repository, times(1)).search(any(), eq("Harry"), any(Pageable.class));
    }

    @Test
    void givenExistingBook_whenGetRelated_thenReturnRelatedBooks() {
        Author author = new Author();
        author.setId(1L);
        author.setName("J.K. Rowling");
        List<BookCardDTO> relatedBooks = List.of(
                new BookCardDTO(2L, "Title 1", "cover", author),
                new BookCardDTO(3L, "Title 2", "cover", author));

        when(repository.findRelated(eq(1L), any())).thenReturn(relatedBooks);

        List<BookCardDTO> result = controller.getRelated(session, 1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).findRelated(eq(1L), any());
    }

    @Test
    void givenDefaultParams_whenGetBooks_thenReturnPage() {
        Page<BookResultDTO> mockPage = new PageImpl<>(List.of(mock(BookResultDTO.class)));
        when(service.getAllBookResults(PageRequest.of(0, 5))).thenReturn(mockPage);

        Page<BookResultDTO> result = controller.getBooks(0, 5);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(service, times(1)).getAllBookResults(PageRequest.of(0, 5));
    }

    @Test
    void givenValidIsbn_whenLookupByIsbn_thenReturn200WithBody() {
        BookLookupDTO dto = new BookLookupDTO();
        when(isbnLookupService.lookup("9780747532743")).thenReturn(Optional.of(dto));

        ResponseEntity<BookLookupDTO> response = controller.lookupByIsbn("9780747532743");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());
        verify(isbnLookupService, times(1)).lookup("9780747532743");
    }

    @Test
    void givenUnknownIsbn_whenLookupByIsbn_thenReturn404() {
        when(isbnLookupService.lookup("0000000000000")).thenReturn(Optional.empty());

        ResponseEntity<BookLookupDTO> response = controller.lookupByIsbn("0000000000000");

        assertEquals(404, response.getStatusCode().value());
        verify(isbnLookupService, times(1)).lookup("0000000000000");
    }

    @Test
    void givenPagesMinGreaterThanMax_whenFilter_thenThrow400() {
        assertThrows(ResponseStatusException.class,
                () -> controller.filter(session, null, null, null, null, null, null, 500, 100, null, null, null, 0, 5));
    }

    @Test
    void givenPagesMinEqualToMax_whenFilter_thenProceedNormally() {
        Page<Book> mockPage = new PageImpl<>(List.of(new Book()));
        Long campusId = 1L;
        when(session.getAttribute("campus")).thenReturn("[1]");

        when(service.filter(any(), any(), any(), any(), any(), any(), eq(Integer.valueOf(200)),
                eq(Integer.valueOf(200)), any(), any(), any(),
                any()))
                .thenReturn(mockPage);

        Page<Book> result = controller.filter(session, campusId, null, null, null, null,
                null, 200, 200, null, null, null,
                0,
                5);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void givenNoParams_whenFilter_thenReturnAllBooks() {
        Long campusId = 1L;
        when(session.getAttribute("campus")).thenReturn("[1]");

        Page<Book> mockPage = new PageImpl<>(List.of(new Book(), new Book(), new Book()));
        when(service.filter(eq(1L), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(mockPage);

        Page<Book> result = controller.filter(session, campusId, null, null, null, null, null,
                null, null, null, null, null, 0, 5);

        assertEquals(3, result.getTotalElements());
        verify(service, times(1)).filter(eq(1L), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), any());
    }

    @Test
    void givenFictionTrue_whenFilter_thenReturnOnlyFictionBooks() {
        Long campusId = 1L;
        when(session.getAttribute("campus")).thenReturn("[1]");

        Book book = new Book();
        book.setTitle("De brief voor de koning");
        book.setFiction(true);
        Page<Book> mockPage = new PageImpl<>(List.of(book));

        when(service.filter(eq(1L), isNull(), isNull(), eq(true), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(mockPage);

        Page<Book> result = controller.filter(session, campusId, null, null, true, null, null,
                null, null, null, null, null, 0, 5);

        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getFiction());
        verify(service, times(1)).filter(eq(1L), isNull(), isNull(), eq(true), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), any());
    }

    @Test
    void givenGenreFilter_whenFilter_thenReturnMatchingBooks() {
        Long campusId = 1L;
        when(session.getAttribute("campus")).thenReturn("[1]");

        Book book = new Book();
        book.setTitle("Harry Potter en de vuurbeker");
        Page<Book> mockPage = new PageImpl<>(List.of(book));

        when(service.filter(eq(1L), eq(List.of(1L)), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(mockPage);

        Page<Book> result = controller.filter(session, campusId, List.of(1L), null, null, null, null,
                null, null, null, null, null, 0, 5);

        assertEquals(1, result.getTotalElements());
        assertEquals("Harry Potter en de vuurbeker", result.getContent().get(0).getTitle());
        verify(service, times(1)).filter(eq(1L), eq(List.of(1L)), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), any());
    }

    @Test
    void givenAuthorFilter_whenFilter_thenReturnMatchingBooks() {
        Long campusId = 1L;
        Book book = new Book();
        book.setTitle("Kruistocht in Spijkerbroek");
        Page<Book> mockPage = new PageImpl<>(List.of(book));

        when(service.filter(eq(1L), isNull(), isNull(), isNull(), eq(List.of(2L)), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(mockPage);

        Page<Book> result = controller.filter(session, campusId, null, null, null, List.of(2L), null,
                null, null, null, null, null, 0, 5);

        assertEquals(1, result.getTotalElements());
        assertEquals("Kruistocht in Spijkerbroek", result.getContent().get(0).getTitle());
        verify(service, times(1)).filter(eq(1L), isNull(), isNull(), isNull(), eq(List.of(2L)), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), any());
    }

    @Test
    void givenSeriesFilter_whenFilter_thenReturnMatchingBooks() {
        Long campusId = 1L;
        Book book = new Book();
        book.setTitle("Harry Potter en de Steen der Wijzen");
        Page<Book> mockPage = new PageImpl<>(List.of(book));

        when(service.filter(eq(1L), isNull(), isNull(), isNull(), isNull(), eq(List.of(1L)),
                isNull(), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(mockPage);

        Page<Book> result = controller.filter(session, campusId, null, null, null, null, List.of(1L),
                null, null, null, null, null, 0, 5);

        assertEquals(1, result.getTotalElements());
        assertEquals("Harry Potter en de Steen der Wijzen", result.getContent().get(0).getTitle());
        verify(service, times(1)).filter(eq(1L), isNull(), isNull(), isNull(), isNull(), eq(List.of(1L)),
                isNull(), isNull(), isNull(), isNull(), isNull(), any());
    }

    @Test
    void givenThemeFilter_whenFilter_thenReturnMatchingBooks() {
        Long campusId = 1L;
        Book book = new Book();
        book.setTitle("De Alchemist");
        Page<Book> mockPage = new PageImpl<>(List.of(book));

        when(service.filter(eq(1L), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), eq(List.of(5L)), isNull(), any()))
                .thenReturn(mockPage);

        Page<Book> result = controller.filter(session, campusId, null, null, null, null, null,
                null, null, null, List.of(5L), null, 0, 5);

        assertEquals(1, result.getTotalElements());
        assertEquals("De Alchemist", result.getContent().get(0).getTitle());
        verify(service, times(1)).filter(eq(1L), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), eq(List.of(5L)), isNull(), any());
    }

    @Test
    void givenPageRangeFilter_whenFilter_thenReturnMatchingBooks() {
        Long campusId = 1L;
        Book book = new Book();
        book.setTitle("De brief voor de koning");
        Page<Book> mockPage = new PageImpl<>(List.of(book));

        when(service.filter(eq(1L), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(100), eq(500), isNull(), isNull(), isNull(), any()))
                .thenReturn(mockPage);

        Page<Book> result = controller.filter(session, campusId, null, null, null, null, null,
                100, 500, null, null, null, 0, 5);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(service, times(1)).filter(eq(1L), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(100), eq(500), isNull(), isNull(), isNull(), any());
    }

    @Test
    void givenClibFilter_whenFilter_thenReturnMatchingBooks() {
        Long campusId = 1L;
        Book book = new Book();
        book.setTitle("Clib Book");
        Page<Book> mockPage = new PageImpl<>(List.of(book));
        List<Clib> clibs = List.of(Clib.A);

        when(service.filter(eq(1L), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq(clibs), isNull(), isNull(), any()))
                .thenReturn(mockPage);

        Page<Book> result = controller.filter(session, campusId, null, null, null, null, null,
                null, null, clibs, null, null, 0, 5);

        assertEquals(1, result.getTotalElements());
        verify(service, times(1)).filter(eq(1L), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq(clibs), isNull(), isNull(), any());
    }

    @Test
    void givenValidCampusString_whenGetLocationIds_thenReturnListOfIds() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("campus")).thenReturn("[1, 2, 3]");

        List<Long> result = controller.getLocationIds(session);

        assertEquals(List.of(1L, 2L, 3L), result);
    }

    @Test
    void givenSingleCampus_whenGetLocationIds_thenReturnSingleItemList() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("campus")).thenReturn("[1]");

        List<Long> result = controller.getLocationIds(session);

        assertEquals(List.of(1L), result);
    }

    @Test
    void givenNullAttribute_whenGetLocationIds_thenThrowException() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("campus")).thenReturn(null);

        assertThrows(NullPointerException.class, () -> controller.getLocationIds(session));
    }

    @Test
    void givenEmptyString_whenGetLocationIds_thenThrowException() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("campus")).thenReturn("[]");

        assertThrows(NumberFormatException.class, () -> controller.getLocationIds(session));
    }
}