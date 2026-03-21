package be.ap.backend.service;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;

import be.ap.backend.dto.CoverDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.repository.BookRepository;

class UploadServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private UploadService uploadService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        uploadService.setUploadDir("test-uploads"); // mock upload directory
    }

    @Test
    void saveCover_nullBook_returnsNull() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        MockMultipartFile file = new MockMultipartFile("file", "cover.png", "image/png", "data".getBytes());
        CoverDTO result = uploadService.saveCover(file, 1L);

        assertNull(result);
        verify(bookRepository, never()).updateCover(anyLong(), anyString());
    }

    @Test
    void saveCover_validBook_returnsCoverDTO() throws IOException {
        Book book = new Book();
        book.setId(1L);
        book.setCover(null);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.updateCover(eq(1L), anyString())).thenReturn(1);

        MockMultipartFile file = new MockMultipartFile("file", "cover.png", "image/png", "data".getBytes());
        CoverDTO cover = uploadService.saveCover(file, 1L);

        assertNotNull(cover);
        assertEquals(1L, cover.getBookId());
        assertTrue(cover.getCover().endsWith(".png"));
        verify(bookRepository).updateCover(eq(1L), anyString());
    }

    @Test
    void saveCover_updateFails_returnsNull() {
        Book book = new Book();
        book.setId(1L);
        book.setCover(null);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.updateCover(eq(1L), anyString())).thenReturn(0);

        MockMultipartFile file = new MockMultipartFile("file", "cover.png", "image/png", "data".getBytes());
        CoverDTO cover = uploadService.saveCover(file, 1L);

        assertNull(cover);
    }

    @Test
    void generateId_lengthIsCorrect() {
        String id = uploadService.generateId();
        assertEquals(32, id.length()); // 16 bytes → 32 hex characters
    }
}