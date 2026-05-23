package be.ap.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import be.ap.backend.dto.BookmarkedDTO;
import be.ap.backend.entity.Author;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Bookmarked;
import be.ap.backend.entity.User;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.BookmarkedRepository;
import be.ap.backend.repository.UserRepository;

@SpringBootTest
public class BookmarkedServiceTest {

    @MockitoBean
    private BookmarkedRepository bookmarkedRepository;

    @MockitoBean
    private BookRepository bookRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private BookmarkedService bookmarkedService;

    @Test
    void getBookmarked_returnsListOfDTOs() {
        Author author = new Author();
        author.setName("J.K. Rowling");

        Book book = new Book();
        book.setId(1L);
        book.setTitle("Harry Potter");
        book.setCover("cover.webp");
        book.setAuthor(author);

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Bookmarked bookmarked = new Bookmarked();
        bookmarked.setId(1L);
        bookmarked.setBook(book);
        bookmarked.setUser(user);

        when(bookmarkedRepository.findByUserIdOrderByAdded(1L)).thenReturn(List.of(bookmarked));

        List<BookmarkedDTO> result = bookmarkedService.getBookmarked(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Harry Potter", result.get(0).title());
        verify(bookmarkedRepository, times(1)).findByUserIdOrderByAdded(1L);
    }

    @Test
    void getBookmarked_emptyList_returnsEmpty() {
        when(bookmarkedRepository.findByUserIdOrderByAdded(99L)).thenReturn(List.of());

        List<BookmarkedDTO> result = bookmarkedService.getBookmarked(99L);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void isBookmarkedd_returnsTrue_whenExists() {
        when(bookmarkedRepository.existsByUserIdAndBookId(1L, 1L)).thenReturn(true);

        assertTrue(bookmarkedService.isBookmarked(1L, 1L));
    }

    @Test
    void isBookmarkedd_returnsFalse_whenNotExists() {
        when(bookmarkedRepository.existsByUserIdAndBookId(1L, 99L)).thenReturn(false);

        assertFalse(bookmarkedService.isBookmarked(1L, 99L));
    }

    @Test
    void toggleBookmarked_removesBookmark_whenExists() {
        when(bookmarkedRepository.existsByUserIdAndBookId(1L, 1L)).thenReturn(true);

        boolean result = bookmarkedService.toggleBookmarked(1L, 1L);

        assertFalse(result);
        verify(bookmarkedRepository, times(1)).deleteByUserIdAndBookId(1L, 1L);
    }

    @Test
    void toggleBookmarked_addsBookmark_whenNotExists() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Book book = new Book();
        book.setId(1L);

        when(bookmarkedRepository.existsByUserIdAndBookId(1L, 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookmarkedRepository.save(any())).thenReturn(new Bookmarked());

        boolean result = bookmarkedService.toggleBookmarked(1L, 1L);

        assertTrue(result);
        verify(bookmarkedRepository, times(1)).save(any());
    }

    @Test
    void toggleBookmarked_throwsException_whenUserNotFound() {
        when(bookmarkedRepository.existsByUserIdAndBookId(1L, 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> bookmarkedService.toggleBookmarked(1L, 1L));
    }

    @Test
    void toggleBookmarked_throwsException_whenBookNotFound() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        when(bookmarkedRepository.existsByUserIdAndBookId(1L, 99L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> bookmarkedService.toggleBookmarked(1L, 99L));
    }
}