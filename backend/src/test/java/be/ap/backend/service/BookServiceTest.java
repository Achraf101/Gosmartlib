package be.ap.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.web.server.ResponseStatusException;

import be.ap.backend.dto.*;
import be.ap.backend.entity.*;
import be.ap.backend.repository.BookRepository;
import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private UploadService uploadService;

    @InjectMocks
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
        when(bookRepository.findThemesForBooks(List.of(1L))).thenReturn(List.of());

        Page<BookResultDTO> result = bookService.getAllBookResults(PageRequest.of(0, 5));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(bookRepository).getAllBookResults(any());
        verify(bookRepository).findGenresForBooks(List.of(1L));
    }

    @Test
    void getAllBookResults_bookWithNoGenres_getsEmptySet() {
        BookResultDTO book = mock(BookResultDTO.class);
        when(book.getId()).thenReturn(99L);
        Page<BookResultDTO> mockPage = new PageImpl<>(List.of(book));
        when(bookRepository.getAllBookResults(any())).thenReturn(mockPage);
        when(bookRepository.findGenresForBooks(List.of(99L))).thenReturn(List.of());
        when(bookRepository.findThemesForBooks(List.of(99L))).thenReturn(List.of());

        bookService.getAllBookResults(PageRequest.of(0, 5));

        verify(book).setGenres(Set.of());
    }

    @Test
    void getAllBookResults_emptyPage_returnsEarlyWithoutQueryingGenresOrThemes() {
        Page<BookResultDTO> emptyPage = new PageImpl<>(List.of());
        when(bookRepository.getAllBookResults(any())).thenReturn(emptyPage);

        Page<BookResultDTO> result = bookService.getAllBookResults(PageRequest.of(0, 5));

        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        verify(bookRepository).getAllBookResults(any());
        verify(bookRepository, never()).findGenresForBooks(any());
        verify(bookRepository, never()).findThemesForBooks(any());
    }

    @Test
    void getAllBookResults_attachesThemesToBooks() {
        BookResultDTO book = mock(BookResultDTO.class);
        when(book.getId()).thenReturn(1L);
        when(bookRepository.getAllBookResults(any())).thenReturn(new PageImpl<>(List.of(book)));
        when(bookRepository.findGenresForBooks(any())).thenReturn(List.of());

        ThemeProjectionDTO theme = mock(ThemeProjectionDTO.class);
        when(theme.getBookId()).thenReturn(1L);
        when(theme.getThemeId()).thenReturn(20L);
        when(theme.getThemeName()).thenReturn("Friendship");
        when(bookRepository.findThemesForBooks(List.of(1L))).thenReturn(List.of(theme));

        bookService.getAllBookResults(PageRequest.of(0, 5));

        verify(bookRepository).findThemesForBooks(List.of(1L));
        verify(book).setThemes(
                argThat(set -> set.stream().anyMatch(t -> t.getId().equals(20L) && t.getName().equals("Friendship"))));
    }

    @Test
    void getAllBookResults_bookWithNoThemes_getsEmptySet() {
        BookResultDTO book = mock(BookResultDTO.class);
        when(book.getId()).thenReturn(2L);
        when(bookRepository.getAllBookResults(any())).thenReturn(new PageImpl<>(List.of(book)));
        when(bookRepository.findGenresForBooks(any())).thenReturn(List.of());
        when(bookRepository.findThemesForBooks(any())).thenReturn(List.of());

        bookService.getAllBookResults(PageRequest.of(0, 5));

        verify(book).setThemes(Set.of());
    }

    @Test
    void getAllBookResults_themesOnlyAttachedToMatchingBook() {
        BookResultDTO book1 = mock(BookResultDTO.class);
        when(book1.getId()).thenReturn(1L);
        BookResultDTO book2 = mock(BookResultDTO.class);
        when(book2.getId()).thenReturn(2L);

        when(bookRepository.getAllBookResults(any())).thenReturn(new PageImpl<>(List.of(book1, book2)));
        when(bookRepository.findGenresForBooks(any())).thenReturn(List.of());

        ThemeProjectionDTO theme = mock(ThemeProjectionDTO.class);
        when(theme.getBookId()).thenReturn(1L);
        when(theme.getThemeId()).thenReturn(30L);
        when(theme.getThemeName()).thenReturn("War");
        when(bookRepository.findThemesForBooks(any())).thenReturn(List.of(theme));

        bookService.getAllBookResults(PageRequest.of(0, 5));

        verify(book1).setThemes(argThat(set -> set.size() == 1));
        verify(book2).setThemes(Set.of());
    }

    @Test
    void filter_emptySeriesIds_passesNullToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.filter(any(), any(), any(), any(), isNull(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        bookService.filter(null, null, null, null, List.of(), null, null, null, null, null, pageable);

        verify(bookRepository).filter(
                isNull(), isNull(), isNull(), isNull(),
                isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(pageable));
    }

    @Test
    void filter_nullSeriesIds_remainsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.filter(any(), any(), any(), any(), isNull(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        bookService.filter(null, null, null, null, null, null, null, null, null, null, pageable);

        verify(bookRepository).filter(
                isNull(), isNull(), isNull(), isNull(),
                isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(pageable));
    }

    @Test
    void filter_nonEmptySeriesIds_passedThroughUnmodified() {
        List<Long> seriesIds = List.of(5L, 6L);
        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.filter(any(), any(), any(), any(), eq(seriesIds), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        bookService.filter(null, null, null, null, seriesIds, null, null, null, null, null, pageable);

        verify(bookRepository).filter(
                isNull(), isNull(), isNull(), isNull(),
                eq(seriesIds),
                isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(pageable));
    }

    @Test
    void saveBook_persistsBookAndReturnsResult() {
        CreateBookDTO dto = new CreateBookDTO();
        dto.setTitle("Dune");
        dto.setBookType(1L);
        dto.setLanguage(2L);
        dto.setFiction(true);

        when(entityManager.find(BookType.class, 1L)).thenReturn(new BookType());
        when(entityManager.find(Language.class, 2L)).thenReturn(new Language());

        Book saved = new Book();
        saved.setTitle("Dune");
        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        Book result = bookService.saveBook(dto);

        assertNotNull(result);
        assertEquals("Dune", result.getTitle());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void saveBook_withCoverUrl_invokesCoverUploadAndSetsField() {
        CreateBookDTO dto = new CreateBookDTO();
        dto.setTitle("Foundation");
        dto.setBookType(1L);
        dto.setLanguage(2L);
        dto.setFiction(true);
        dto.setCoverUrl("https://example.com/cover.jpg");

        when(entityManager.find(BookType.class, 1L)).thenReturn(new BookType());
        when(entityManager.find(Language.class, 2L)).thenReturn(new Language());
        when(uploadService.saveCoverFromUrl("https://example.com/cover.jpg")).thenReturn("cover_saved.jpg");
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.saveBook(dto);

        verify(uploadService).saveCoverFromUrl("https://example.com/cover.jpg");
        verify(bookRepository).save(argThat(b -> "cover_saved.jpg".equals(b.getCover())));
    }

    @Test
    void saveBook_withBlankCoverUrl_doesNotInvokeCoverUpload() {
        CreateBookDTO dto = new CreateBookDTO();
        dto.setTitle("Neuromancer");
        dto.setBookType(1L);
        dto.setLanguage(2L);
        dto.setFiction(true);
        dto.setCoverUrl("   ");

        when(entityManager.find(BookType.class, 1L)).thenReturn(new BookType());
        when(entityManager.find(Language.class, 2L)).thenReturn(new Language());
        when(bookRepository.save(any(Book.class))).thenReturn(new Book());

        bookService.saveBook(dto);

        verify(uploadService, never()).saveCoverFromUrl(any());
    }

    @Test
    void saveBook_withSeriesCountZero_doesNotSetSeriesNumber() {
        CreateBookDTO dto = new CreateBookDTO();
        dto.setTitle("Standalone");
        dto.setBookType(1L);
        dto.setLanguage(2L);
        dto.setFiction(false);
        dto.setSeriesCount(0);

        when(entityManager.find(BookType.class, 1L)).thenReturn(new BookType());
        when(entityManager.find(Language.class, 2L)).thenReturn(new Language());
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.saveBook(dto);

        verify(bookRepository).save(argThat(b -> b.getSeriesNumber() == null));
    }

    @Test
    void saveBook_withGenresAndThemes_mapsEntitiesCorrectly() {
        CreateBookDTO dto = new CreateBookDTO();
        dto.setTitle("The Name of the Wind");
        dto.setBookType(1L);
        dto.setLanguage(2L);
        dto.setFiction(true);
        dto.setGenres(List.of(10L, 11L));
        dto.setThemes(List.of(20L));

        when(entityManager.find(eq(BookType.class), any())).thenReturn(new BookType());
        when(entityManager.find(eq(Language.class), any())).thenReturn(new Language());

        Genre genre1 = new Genre();
        genre1.setId(10L);
        Genre genre2 = new Genre();
        genre2.setId(11L);
        when(entityManager.find(eq(Genre.class), eq(10L))).thenReturn(genre1);
        when(entityManager.find(eq(Genre.class), eq(11L))).thenReturn(genre2);

        Theme theme1 = new Theme();
        theme1.setId(20L);
        when(entityManager.find(eq(Theme.class), eq(20L))).thenReturn(theme1);

        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.saveBook(dto);

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        Book saved = captor.getValue();

        assertNotNull(saved.getGenres());
        assertEquals(2, saved.getGenres().size());
        assertNotNull(saved.getThemes());
        assertEquals(1, saved.getThemes().size());
    }

    @Test
    void updateBook_titleOnly_updatesTitle() {
        Book existingBook = new Book();
        existingBook.setTitle("Original Title");
        existingBook.setIsbn("1234567890");
        existingBook.setPages(100);
        existingBook.setFiction(true);
        existingBook.setDidactic(false);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateBookDTO dto = new UpdateBookDTO();
        dto.setTitle("New Title");

        Book result = bookService.updateBook(1L, dto);

        assertEquals("New Title", result.getTitle());
        assertEquals("1234567890", result.getIsbn());
    }

    @Test
    void updateBook_allSimpleFields_updatesAll() {
        Book existingBook = new Book();
        existingBook.setTitle("Original Title");
        existingBook.setIsbn("1234567890");
        existingBook.setPages(100);
        existingBook.setFiction(true);
        existingBook.setDidactic(false);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateBookDTO dto = new UpdateBookDTO();
        dto.setTitle("Updated");
        dto.setIsbn("9876543210");
        dto.setDescription("New desc");
        dto.setFiction(false);
        dto.setDidactic(true);
        dto.setPages(200);
        dto.setCover("cover.jpg");
        dto.setFontSize(FontSize.GROOT);
        dto.setClib(Clib.A);
        dto.setSeriesNumber(3);

        Book result = bookService.updateBook(1L, dto);

        assertEquals("Updated", result.getTitle());
        assertEquals("9876543210", result.getIsbn());
        assertEquals("New desc", result.getDescription());
        assertFalse(result.getFiction());
        assertTrue(result.getDidactic());
        assertEquals(200, result.getPages());
        assertEquals("cover.jpg", result.getCover());
        assertEquals(FontSize.GROOT, result.getFontSize());
        assertEquals(Clib.A, result.getClib());
        assertEquals(3, result.getSeriesNumber());
    }

    @Test
    void updateBook_nullFields_doesNotOverwrite() {
        Book existingBook = new Book();
        existingBook.setTitle("Original Title");
        existingBook.setIsbn("1234567890");
        existingBook.setPages(100);
        existingBook.setFiction(true);
        existingBook.setDidactic(false);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.updateBook(1L, new UpdateBookDTO());

        assertEquals("Original Title", result.getTitle());
        assertEquals("1234567890", result.getIsbn());
        assertEquals(100, result.getPages());
    }

    @Test
    void updateBook_withAuthor_setsAuthor() {
        Book existingBook = new Book();
        existingBook.setTitle("Original Title");
        existingBook.setIsbn("1234567890");
        existingBook.setPages(100);
        existingBook.setFiction(true);
        existingBook.setDidactic(false);

        Author author = new Author();
        author.setName("Test Author");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(entityManager.find(Author.class, 5L)).thenReturn(author);
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateBookDTO dto = new UpdateBookDTO();
        dto.setAuthor(5L);

        Book result = bookService.updateBook(1L, dto);

        assertEquals(author, result.getAuthor());
    }

    @Test
    void updateBook_withGenres_setsGenres() {
        Book existingBook = new Book();
        existingBook.setTitle("Original Title");
        existingBook.setIsbn("1234567890");
        existingBook.setPages(100);
        existingBook.setFiction(true);
        existingBook.setDidactic(false);

        Genre g1 = new Genre(); g1.setId(1L);
        Genre g2 = new Genre(); g2.setId(2L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(entityManager.find(Genre.class, 1L)).thenReturn(g1);
        when(entityManager.find(Genre.class, 2L)).thenReturn(g2);
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateBookDTO dto = new UpdateBookDTO();
        dto.setGenres(Set.of(1L, 2L));

        Book result = bookService.updateBook(1L, dto);

        assertEquals(2, result.getGenres().size());
        assertTrue(result.getGenres().containsAll(Set.of(g1, g2)));
    }

    @Test
    void updateBook_withThemes_setsThemes() {
        Book existingBook = new Book();
        existingBook.setTitle("Original Title");
        existingBook.setIsbn("1234567890");
        existingBook.setPages(100);
        existingBook.setFiction(true);
        existingBook.setDidactic(false);

        Theme t1 = new Theme(); t1.setId(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(entityManager.find(Theme.class, 1L)).thenReturn(t1);
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateBookDTO dto = new UpdateBookDTO();
        dto.setThemes(Set.of(1L));

        Book result = bookService.updateBook(1L, dto);

        assertEquals(1, result.getThemes().size());
    }

    @Test
    void updateBook_bookNotFound_throwsNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> bookService.updateBook(99L, new UpdateBookDTO()));
    }

    @Test
    void updateBook_saveIsCalledOnce() {
        Book existingBook = new Book();
        existingBook.setTitle("Original Title");
        existingBook.setIsbn("1234567890");
        existingBook.setPages(100);
        existingBook.setFiction(true);
        existingBook.setDidactic(false);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        bookService.updateBook(1L, new UpdateBookDTO());

        verify(bookRepository, times(1)).save(existingBook);
    }
}