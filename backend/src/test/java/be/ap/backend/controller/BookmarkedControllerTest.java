package be.ap.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import be.ap.backend.dto.BookmarkedDTO;
import be.ap.backend.entity.Author;
import be.ap.backend.service.BookmarkedService;

@SpringBootTest
public class BookmarkedControllerTest {

    @MockitoBean
    private BookmarkedService bookmarkedService;

    @Autowired
    private BookmarkedController bookmarkedController;

    @Test
    void getBookmarkeds_returnsListOfDTOs() {
        Author author = new Author();
            author.setId(1L);
            author.setName("J.K. Rowling");
            author.setDescription("British author");
        BookmarkedDTO dto = new BookmarkedDTO(1L, 1L, "Harry Potter", "cover.webp", author, null);
        when(bookmarkedService.getBookmarked(1L)).thenReturn(List.of(dto));

        List<BookmarkedDTO> result = bookmarkedController.getBookmarkeds(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Harry Potter", result.get(0).title());
        verify(bookmarkedService, times(1)).getBookmarked(1L);
    }

    @Test
    void getBookmarkeds_emptyList_returnsEmpty() {
        when(bookmarkedService.getBookmarked(99L)).thenReturn(List.of());

        List<BookmarkedDTO> result = bookmarkedController.getBookmarkeds(99L);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void isBookmarkedd_returnsTrue() {
        when(bookmarkedService.isBookmarked(1L, 1L)).thenReturn(true);

        assertTrue(bookmarkedController.isBookmarked(1L, 1L));
    }

    @Test
    void isBookmarkedd_returnsFalse() {
        when(bookmarkedService.isBookmarked(1L, 99L)).thenReturn(false);

        assertFalse(bookmarkedController.isBookmarked(1L, 99L));
    }

    @Test
    void toggleBookmarked_returnsTrue_whenAdded() {
        when(bookmarkedService.toggleBookmarked(1L, 1L)).thenReturn(true);

        assertTrue(bookmarkedController.toggleBookmarked(1L, 1L));
        verify(bookmarkedService, times(1)).toggleBookmarked(1L, 1L);
    }

    @Test
    void toggleBookmarked_returnsFalse_whenRemoved() {
        when(bookmarkedService.toggleBookmarked(1L, 1L)).thenReturn(false);

        assertFalse(bookmarkedController.toggleBookmarked(1L, 1L));
        verify(bookmarkedService, times(1)).toggleBookmarked(1L, 1L);
    }
}