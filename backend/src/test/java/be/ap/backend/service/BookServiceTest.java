package be.ap.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import be.ap.backend.dto.GenreProjection;
import be.ap.backend.dto.BookResultDTO;
import be.ap.backend.repository.BookRepository;

@SpringBootTest
public class BookServiceTest {
    @MockitoBean
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;

    @Test
    void getAllBookResults_attachesGenresToBooks() {
        BookResultDTO book = mock(BookResultDTO.class);
        when(book.getId()).thenReturn(1L);

        Page<BookResultDTO> mockPage = new PageImpl<>(List.of(book));
        when(bookRepository.getAllBookResults(any())).thenReturn(mockPage);

        GenreProjection projection = mock(GenreProjection.class);
        when(projection.getBookId()).thenReturn(1L);
        when(projection.getGenreId()).thenReturn(10L);
        when(projection.getGenreName()).thenReturn("Fantasy");
        when(bookRepository.findGenresForBooks(List.of(1L))).thenReturn(List.of(projection));

        Page<BookResultDTO> result = bookService.getAllBookResults(PageRequest.of(0, 5));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(bookRepository, times(1)).getAllBookResults(any());
        verify(bookRepository, times(1)).findGenresForBooks(List.of(1L));
    }

    @Test
    void getAllBookResults_bookWithNoGenres_getsEmptySet() {
        BookResultDTO book = mock(BookResultDTO.class);
        when(book.getId()).thenReturn(99L);

        Page<BookResultDTO> mockPage = new PageImpl<>(List.of(book));
        when(bookRepository.getAllBookResults(any())).thenReturn(mockPage);
        when(bookRepository.findGenresForBooks(List.of(99L))).thenReturn(List.of());

        Page<BookResultDTO> result = bookService.getAllBookResults(PageRequest.of(0, 5));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(book).setGenres(Set.of());
    }

    @Test
    void getAllBookResults_emptyPage_returnsEmpty() {
        Page<BookResultDTO> emptyPage = new PageImpl<>(List.of());
        when(bookRepository.getAllBookResults(any())).thenReturn(emptyPage);

        Page<BookResultDTO> result = bookService.getAllBookResults(PageRequest.of(0, 5));

        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        verify(bookRepository, times(1)).getAllBookResults(any());
        verify(bookRepository, times(0)).findGenresForBooks(any());
    }
}
