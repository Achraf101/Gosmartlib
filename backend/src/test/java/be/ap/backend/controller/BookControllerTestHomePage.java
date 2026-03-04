package be.ap.backend.controller;

import be.ap.backend.entity.Book;
import be.ap.backend.service.BookService;
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
public class BookControllerTestHomePage {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    @Test
    public void getFeaturedBooks_returnsStatus200() {
        when(bookService.getFeaturedBooks()).thenReturn(Collections.emptyList());

        List<Book> result = bookController.getFeaturedBooks();

        assertNotNull(result);
    }

    @Test
    public void getFeaturedBooks_returnsList() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Het TikTok Kamp");

        when(bookService.getFeaturedBooks()).thenReturn(Arrays.asList(book));

        List<Book> result = bookController.getFeaturedBooks();

        assertEquals(1, result.size());
        assertEquals("Het TikTok Kamp", result.get(0).getTitle());
    }
}