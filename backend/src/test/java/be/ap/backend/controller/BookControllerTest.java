package be.ap.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

        when(repository.findBy()).thenReturn(List.of(book1, book2));

        List<Book> result = controller.getAll();

        assertEquals(2, result.size());
        assertEquals("Book 1", result.get(0).getTitle());
        assertEquals("Book 2", result.get(1).getTitle());

        verify(repository, times(1)).findBy();
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
}
