package be.ap.backend.service;

import be.ap.backend.entity.Book;
import be.ap.backend.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTestHomePage {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    public void getFeaturedBooks_returnsMaxFourBooks() {
        List<Book> mockBooks = Arrays.asList(
            createBook(1L, "Boek 1"),
            createBook(2L, "Boek 2"),
            createBook(3L, "Boek 3"),
            createBook(4L, "Boek 4"),
            createBook(5L, "Boek 5")
        );
        when(bookRepository.findAll()).thenReturn(mockBooks);

        List<Book> result = bookService.getFeaturedBooks();

        assertEquals(4, result.size());
    }

    @Test
    public void getFeaturedBooks_returnsEmptyList_whenNoBooksInDatabase() {
        when(bookRepository.findAll()).thenReturn(Collections.emptyList());

        List<Book> result = bookService.getFeaturedBooks();

        assertTrue(result.isEmpty());
    }

    private Book createBook(Long id, String title) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        return book;
    }
}