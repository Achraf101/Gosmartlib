package be.ap.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import be.ap.backend.dto.CreateBookDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.service.BookService;

@SpringBootTest
public class BookControllerTest {

    @MockitoBean
    private BookService service;

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
}
