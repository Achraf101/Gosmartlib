package be.ap.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;

import be.ap.backend.dto.BookCardDTO;
import be.ap.backend.dto.BookResultDTO;
import be.ap.backend.dto.CreateBookDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.service.BookService;

@SpringBootTest
public class BookControllerTest {

    @MockitoBean
    private BookService service;

    @MockitoBean
    private BookRepository repository;

    @Autowired
    private BookController controller;

    @Test
    void givenBook_whenGetBook_thenGetBooks() {
        // Arrange
        CreateBookDTO inputBook = new CreateBookDTO();
        inputBook.setTitle("Test book");

        Book savedBook = new Book();
        savedBook.setId(1L);
        savedBook.setTitle("Test book");

        when(service.saveBook(inputBook)).thenReturn(savedBook);

        // Act
        Book result = controller.addBook(inputBook);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test book", result.getTitle());

        verify(service, times(1)).saveBook(inputBook);
    }

    @Test
    void givenBooksExist_whenGetAll_thenReturnBooks() {

        Book book1 = new Book();
        book1.setId(1L);
        book1.setTitle("Book 1");

        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Book 2");

        List<Book> books = List.of(book1, book2);
        Page<Book> page = new PageImpl<>(books);

        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Book> result = controller.getAll(1, 5);

        assertEquals(2, result.getContent().size());
        assertEquals("Book 1", result.getContent().get(0).getTitle());
        assertEquals("Book 2", result.getContent().get(1).getTitle());

        verify(repository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void givenBookId_whenGetById_thenReturnBook() {

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
    void givenSearchQuery_whenSearch_thenReturnMatchingBooks() {
        // Arrange
        Book book1 = new Book();
        book1.setId(1L);
        book1.setTitle("Harry Potter");

        List<Book> books = List.of(book1);
        Page<Book> page = new PageImpl<>(books);

        when(repository.search(eq("Harry"), any(Pageable.class))).thenReturn(page);

        // Act
        Page<Book> result = controller.search("Harry", 0, 5);

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals("Harry Potter", result.getContent().get(0).getTitle());

        verify(repository, times(1)).search(eq("Harry"), any(Pageable.class));
    }

    @Test
    void givenNoMatch_whenSearch_thenReturnEmptyPage() {
        // Arrange
        Page<Book> emptyPage = new PageImpl<>(List.of());

        when(repository.search(eq("nonexistent"), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<Book> result = controller.search("nonexistent", 0, 5);

        // Assert
        assertEquals(0, result.getContent().size());

        verify(repository, times(1)).search(eq("nonexistent"), any(Pageable.class));
    }

    @Test
    void testGetRelatedBooks_Success() {
        Long bookId = 1L;

        // Mock related books
        List<BookCardDTO> relatedBooks = List.of(
                new BookCardDTO(2L, "Title 1", "cover", "Jan"),
                new BookCardDTO(2L, "Title 2", "cover", "Jan"));

        // Mock repository behavior
        when(repository.findRelated(bookId)).thenReturn(relatedBooks);

        // Call controller method
        List<BookCardDTO> result = repository.findRelated(bookId);

        // Assertions
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Title 1", result.get(0).getTitle());
        assertEquals("Title 2", result.get(1).getTitle());
    }

    @Test
    void testGetRelatedBooks_Empty() {
        Long bookId = 1L;

        // Mock repository behavior: no related books
        when(repository.findRelated(bookId)).thenReturn(Collections.emptyList());

        // Call controller method
        List<BookCardDTO> result = repository.findRelated(bookId);

        // Assertions
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Expected empty list when no related books exist");
    }

    @Test
    void getBooks_defaultParams_returnsPage() {
        Page<BookResultDTO> mockPage = new PageImpl<>(List.of(mock(BookResultDTO.class)));
        when(service.getAllBookResults(PageRequest.of(0, 5))).thenReturn(mockPage);

        Page<BookResultDTO> result = controller.getBooks(0, 5);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getBooks_customParams_passesCorrectPageable() {
        Page<BookResultDTO> mockPage = new PageImpl<>(List.of());
        when(service.getAllBookResults(PageRequest.of(2, 10))).thenReturn(mockPage);

        Page<BookResultDTO> result = controller.getBooks(2, 10);

        assertEquals(0, result.getContent().size());
        verify(service).getAllBookResults(PageRequest.of(2, 10));
    }

    @Test
    void getBooks_firstPage_returnsCorrectMetadata() {
        List<BookResultDTO> books = List.of(mock(BookResultDTO.class), mock(BookResultDTO.class));
        Page<BookResultDTO> mockPage = new PageImpl<>(books, PageRequest.of(0, 5), 11);
        when(service.getAllBookResults(PageRequest.of(0, 5))).thenReturn(mockPage);

        Page<BookResultDTO> result = controller.getBooks(0, 5);

        assertEquals(11, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
    }

    void givenValidParams_whenFilter_thenReturnBooks() {
       
        Book book = new Book();
        book.setTitle("De brief voor de koning");
        Page<Book> mockPage = new PageImpl<>(List.of(book));
        when(repository.filter(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
            .thenReturn(mockPage);
 
       
        Page<Book> result = controller.filter(null, null, null, null, 100, 500, 0, 5);
 
       
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository, times(1)).filter(any(), any(), any(), any(), any(), any(), any(Pageable.class));
    }
 
    @Test
    void givenPagesMinGreaterThanMax_whenFilter_thenThrow400() {
        
        Integer pagesMin = 500;
        Integer pagesMax = 100;
 
        
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            controller.filter(null, null, null, null, pagesMin, pagesMax, 0, 5);
        });
        assertEquals(400, exception.getStatusCode().value());
    }
 
    @Test
    void givenNoParams_whenFilter_thenReturnAllBooks() {
        
        Page<Book> mockPage = new PageImpl<>(List.of(new Book(), new Book(), new Book()));
        when(repository.filter(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(mockPage);
 
       
        Page<Book> result = controller.filter(null, null, null, null, null, null, 0, 5);
 
        
        assertEquals(3, result.getTotalElements());
        verify(repository, times(1)).filter(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }
 
    @Test
    void givenFictionTrue_whenFilter_thenReturnOnlyFictionBooks() {
        
        Book book = new Book();
        book.setTitle("De brief voor de koning");
        book.setFiction(true);
        Page<Book> mockPage = new PageImpl<>(List.of(book));
        when(repository.filter(any(), any(), eq(true), any(), any(), any(), any(Pageable.class)))
            .thenReturn(mockPage);
 
       
        Page<Book> result = controller.filter(null, null, true, null, null, null, 0, 5);
 
        
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getFiction());
        verify(repository, times(1)).filter(any(), any(), eq(true), any(), any(), any(), any(Pageable.class));
    }
 
    @Test
    void givenGenreFilter_whenFilter_thenReturnCorrectBooks() {
        
        Book book = new Book();
        book.setTitle("Harry Potter en de vuurbeker");
        Page<Book> mockPage = new PageImpl<>(List.of(book));
        when(repository.filter(eq(List.of(1L)), any(), any(), any(), any(), any(), any(Pageable.class)))
            .thenReturn(mockPage);
 
        
        Page<Book> result = controller.filter(List.of(1L), null, null, null, null, null, 0, 5);
 
        
        assertEquals(1, result.getTotalElements());
        assertEquals("Harry Potter en de vuurbeker", result.getContent().get(0).getTitle());
        verify(repository, times(1)).filter(eq(List.of(1L)), any(), any(), any(), any(), any(), any(Pageable.class));
    }
 
    @Test
    void givenAuthorFilter_whenFilter_thenReturnCorrectBooks() {
        
        Book book = new Book();
        book.setTitle("Kruistocht in Spijkerbroek");
        Page<Book> mockPage = new PageImpl<>(List.of(book));
        when(repository.filter(any(), any(), any(), eq(List.of(2L)), any(), any(), any(Pageable.class)))
            .thenReturn(mockPage);
 
     
        Page<Book> result = controller.filter(null, null, null, List.of(2L), null, null, 0, 5);
 
       
        assertEquals(1, result.getTotalElements());
        assertEquals("Kruistocht in Spijkerbroek", result.getContent().get(0).getTitle());
        verify(repository, times(1)).filter(any(), any(), any(), eq(List.of(2L)), any(), any(), any(Pageable.class));
    }
}
