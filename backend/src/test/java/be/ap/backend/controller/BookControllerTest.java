package be.ap.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import be.ap.backend.dto.BookCardDTO;
import be.ap.backend.dto.BookLookupDTO;
import be.ap.backend.dto.BookResultDTO;
import be.ap.backend.dto.CreateBookDTO;
import be.ap.backend.dto.UpdateBookDTO;
import be.ap.backend.entity.Author;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Clib;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.service.BookService;
import be.ap.backend.service.IsbnLookupService;

@ExtendWith(MockitoExtension.class)
public class BookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookService service;

    @Mock
    private BookRepository repository;

    @Mock
    private IsbnLookupService isbnLookupService;

    @InjectMocks
    private BookController controller;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
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
        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Book> result = controller.getAll(0, 5);

        assertEquals(2, result.getContent().size());
        assertEquals("Book 1", result.getContent().get(0).getTitle());
        assertEquals("Book 2", result.getContent().get(1).getTitle());
        verify(repository, times(1)).findAll(any(Pageable.class));
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
        when(repository.search(eq("Harry"), any(Pageable.class))).thenReturn(page);

        Page<Book> result = controller.search("Harry", 0, 5);

        assertEquals(1, result.getContent().size());
        assertEquals("Harry Potter", result.getContent().get(0).getTitle());
        verify(repository, times(1)).search(eq("Harry"), any(Pageable.class));
    }

    @Test
    void givenExistingBook_whenGetRelated_thenReturnRelatedBooks() {
        Author author = new Author();
        author.setId(1L);
        author.setName("J.K. Rowling");
        List<BookCardDTO> relatedBooks = List.of(
                new BookCardDTO(2L, "Title 1", "cover", author),
                new BookCardDTO(3L, "Title 2", "cover", author));

        when(repository.findRelated(1L)).thenReturn(relatedBooks);

        List<BookCardDTO> result = controller.getRelated(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).findRelated(1L);
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
                () -> controller.filter(null, null, null, null, null, 500, 100, null, null, null, 0, 5));
    }

    @Test
    void givenPagesMinEqualToMax_whenFilter_thenProceedNormally() {
        Page<Book> mockPage = new PageImpl<>(List.of(new Book()));
        when(service.filter(any(), any(), any(), any(), any(), eq(200), eq(200), any(), any(), any(),
                any(Pageable.class)))
                .thenReturn(mockPage);

        Page<Book> result = controller.filter(null, null, null, null, null, 200, 200, null, null, null, 0, 5);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void givenNoParams_whenFilter_thenReturnAllBooks() {
        Page<Book> mockPage = new PageImpl<>(List.of(new Book(), new Book(), new Book()));
        when(service.filter(isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(mockPage);

        Page<Book> result = controller.filter(null, null, null, null, null,
                null, null, null, null, null, 0, 5);

        assertEquals(3, result.getTotalElements());
        verify(service, times(1)).filter(isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void givenFictionTrue_whenFilter_thenReturnOnlyFictionBooks() {
        Book book = new Book();
        book.setTitle("De brief voor de koning");
        book.setFiction(true);

        Page<Book> mockPage = new PageImpl<>(List.of(book));
        when(service.filter(any(), any(), eq(true), any(), any(), any(), any(), any(), any(), any(),
                any(Pageable.class)))
                .thenReturn(mockPage);

        Page<Book> result = controller.filter(null, null, true, null, null,
                null, null, null, null, null, 0, 5);

        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getFiction());
        verify(service, times(1)).filter(any(), any(), eq(true), any(), any(),
                any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void givenGenreFilter_whenFilter_thenReturnMatchingBooks() {
        Book book = new Book();
        book.setTitle("Harry Potter en de vuurbeker");

        Page<Book> mockPage = new PageImpl<>(List.of(book));
        when(service.filter(eq(List.of(1L)), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(Pageable.class)))
                .thenReturn(mockPage);

        Page<Book> result = controller.filter(List.of(1L), null, null, null, null,
                null, null, null, null, null, 0, 5);

        assertEquals(1, result.getTotalElements());
        assertEquals("Harry Potter en de vuurbeker", result.getContent().get(0).getTitle());
        verify(service, times(1)).filter(eq(List.of(1L)), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void givenAuthorFilter_whenFilter_thenReturnMatchingBooks() {
        Book book = new Book();
        book.setTitle("Kruistocht in Spijkerbroek");

        Page<Book> mockPage = new PageImpl<>(List.of(book));
        when(service.filter(any(), any(), any(), eq(List.of(2L)), any(), any(), any(), any(), any(), any(),
                any(Pageable.class)))
                .thenReturn(mockPage);

        Page<Book> result = controller.filter(null, null, null, List.of(2L), null,
                null, null, null, null, null, 0, 5);

        assertEquals(1, result.getTotalElements());
        assertEquals("Kruistocht in Spijkerbroek", result.getContent().get(0).getTitle());
        verify(service, times(1)).filter(any(), any(), any(), eq(List.of(2L)), any(),
                any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void givenSeriesFilter_whenFilter_thenReturnMatchingBooks() {
        Book book = new Book();
        book.setTitle("Harry Potter en de Steen der Wijzen");

        Page<Book> mockPage = new PageImpl<>(List.of(book));
        when(service.filter(any(), any(), any(), any(), eq(List.of(1L)), any(), any(), any(), any(), any(),
                any(Pageable.class)))
                .thenReturn(mockPage);

        Page<Book> result = controller.filter(null, null, null, null, List.of(1L),
                null, null, null, null, null, 0, 5);

        assertEquals(1, result.getTotalElements());
        assertEquals("Harry Potter en de Steen der Wijzen", result.getContent().get(0).getTitle());
        verify(service, times(1)).filter(any(), any(), any(), any(), eq(List.of(1L)),
                any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void givenThemeFilter_whenFilter_thenReturnMatchingBooks() {
        Book book = new Book();
        book.setTitle("De Alchemist");

        Page<Book> mockPage = new PageImpl<>(List.of(book));
        when(service.filter(any(), any(), any(), any(), any(), any(), any(), any(), eq(List.of(5L)), any(),
                any(Pageable.class)))
                .thenReturn(mockPage);

        Page<Book> result = controller.filter(null, null, null, null, null,
                null, null, null, List.of(5L), null, 0, 5);

        assertEquals(1, result.getTotalElements());
        assertEquals("De Alchemist", result.getContent().get(0).getTitle());
        verify(service, times(1)).filter(any(), any(), any(), any(), any(),
                any(), any(), any(), eq(List.of(5L)), any(), any(Pageable.class));
    }

    @Test
    void givenPageRangeFilter_whenFilter_thenReturnMatchingBooks() {
        Book book = new Book();
        book.setTitle("De brief voor de koning");

        Page<Book> mockPage = new PageImpl<>(List.of(book));
        when(service.filter(any(), any(), any(), any(), any(), eq(100), eq(500), any(), any(),
                any(),
                any(Pageable.class)))
                .thenReturn(mockPage);

        Page<Book> result = controller.filter(null, null, null, null, null,
                100, 500, null, null, null, 0, 5);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(service, times(1)).filter(any(), any(), any(), any(), any(),
                eq(100), eq(500), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void givenClibFilter_whenFilter_thenReturnMatchingBooks() {
        Book book = new Book();
        book.setTitle("Clib Book");

        Page<Book> mockPage = new PageImpl<>(List.of(book));
        List<Clib> clibs = List.of(Clib.A);

        when(service.filter(any(), any(), any(), any(), any(), any(), any(), eq(clibs), any(), any(),
                any(Pageable.class)))
                .thenReturn(mockPage);

        Page<Book> result = controller.filter(null, null, null, null, null,
                null, null, clibs, null, null, 0, 5);

        assertEquals(1, result.getTotalElements());
        verify(service, times(1)).filter(any(), any(), any(), any(), any(),
                any(), any(), eq(clibs), any(), any(), any(Pageable.class));
    }

    @Test
    void updateBook_validRequest_returns200() throws Exception {
        Book updatedBook = new Book();
        updatedBook.setTitle("Updated Title");

        when(service.updateBook(eq(1L), any(UpdateBookDTO.class))).thenReturn(updatedBook);

        UpdateBookDTO dto = new UpdateBookDTO();
        dto.setTitle("Updated Title");

        mockMvc.perform(put("/book/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void updateBook_bookNotFound_returns404() throws Exception {
        when(service.updateBook(eq(99L), any(UpdateBookDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(put("/book/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateBookDTO())))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBook_emptyBody_returns200() throws Exception {
        when(service.updateBook(eq(1L), any(UpdateBookDTO.class))).thenReturn(new Book());

        mockMvc.perform(put("/book/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateBook_serviceCalledWithCorrectId() throws Exception {
        when(service.updateBook(eq(1L), any(UpdateBookDTO.class))).thenReturn(new Book());

        mockMvc.perform(put("/book/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

        verify(service, times(1)).updateBook(eq(1L), any(UpdateBookDTO.class));
    }
}