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
import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.*;
import be.ap.backend.entity.*;
import be.ap.backend.enums.Clib;
import be.ap.backend.enums.FontSize;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.LocationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private UploadService uploadService;

    @Mock
    private OpenLibraryService openLibraryService;

    @Mock
    private SessionContext sessionContext;

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private BookService bookService;

    // -------------------------------------------------------------------------
    // getAllBookResults
    // -------------------------------------------------------------------------

    @Test
    void getAllBookResults_attachesGenresToBooks() {
        BookResultDTO book = mock(BookResultDTO.class);
        when(book.getId()).thenReturn(1L);
        Page<BookResultDTO> mockPage = new PageImpl<>(List.of(book));
        when(bookRepository.getAllBookResults(any())).thenReturn(mockPage);

        GenreProjectionDTO projection = mock(GenreProjectionDTO.class);
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
                argThat(set -> set.stream().anyMatch(t -> t.getId().equals(20L) &&
                        t.getName().equals("Friendship"))));
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
    void getAllBookResults_genresOnlyAttachedToMatchingBook() {
        BookResultDTO book1 = mock(BookResultDTO.class);
        when(book1.getId()).thenReturn(1L);
        BookResultDTO book2 = mock(BookResultDTO.class);
        when(book2.getId()).thenReturn(2L);

        when(bookRepository.getAllBookResults(any())).thenReturn(new PageImpl<>(List.of(book1, book2)));
        when(bookRepository.findThemesForBooks(any())).thenReturn(List.of());

        GenreProjectionDTO genre = mock(GenreProjectionDTO.class);
        when(genre.getBookId()).thenReturn(2L);
        when(genre.getGenreId()).thenReturn(50L);
        when(genre.getGenreName()).thenReturn("Thriller");
        when(bookRepository.findGenresForBooks(any())).thenReturn(List.of(genre));

        bookService.getAllBookResults(PageRequest.of(0, 5));

        verify(book1).setGenres(Set.of());
        verify(book2).setGenres(argThat(set -> set.size() == 1));
    }

    @Test
    void getAllBookResults_multipleGenresAttachedToSameBook() {
        BookResultDTO book = mock(BookResultDTO.class);
        when(book.getId()).thenReturn(1L);
        when(bookRepository.getAllBookResults(any())).thenReturn(new PageImpl<>(List.of(book)));
        when(bookRepository.findThemesForBooks(any())).thenReturn(List.of());

        GenreProjectionDTO genre1 = mock(GenreProjectionDTO.class);
        when(genre1.getBookId()).thenReturn(1L);
        when(genre1.getGenreId()).thenReturn(10L);
        when(genre1.getGenreName()).thenReturn("Fantasy");

        GenreProjectionDTO genre2 = mock(GenreProjectionDTO.class);
        when(genre2.getBookId()).thenReturn(1L);
        when(genre2.getGenreId()).thenReturn(11L);
        when(genre2.getGenreName()).thenReturn("Adventure");

        when(bookRepository.findGenresForBooks(any())).thenReturn(List.of(genre1, genre2));

        bookService.getAllBookResults(PageRequest.of(0, 5));

        verify(book).setGenres(argThat(set -> set.size() == 2));
    }

    // -------------------------------------------------------------------------
    // filter
    // -------------------------------------------------------------------------

    @Test
    void filter_emptySeriesIds_passesNullToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        when(sessionContext.hasRole(any())).thenReturn(true);
        when(bookRepository.filterAdmin(any(), any(), any(), any(), isNull(), any(),
                any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        bookService.filter(null, true, null, null, null, null, null, List.of(), null,
                null, null, null, null, null, pageable);

        verify(bookRepository).filterAdmin(isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(pageable));
    }

    @Test
    void filter_nullSeriesIds_remainsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        when(sessionContext.hasRole(any())).thenReturn(true);
        when(bookRepository.filterAdmin(any(), any(), any(), any(), isNull(), any(),
                any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        bookService.filter(null, true, null, null, null, null, null, null, null,
                null, null, null, null, null, pageable);

        verify(bookRepository).filterAdmin(isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(pageable));
    }

    @Test
    void filter_nonEmptySeriesIds_passedThroughUnmodified() {
        List<Long> seriesIds = List.of(5L, 6L);
        Pageable pageable = PageRequest.of(0, 10);
        when(sessionContext.hasRole(any())).thenReturn(true);
        when(bookRepository.filterAdmin(any(), any(), any(), any(), eq(seriesIds),
                any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        bookService.filter(null, true, null, null, null, null, null, seriesIds, null,
                null, null, null, null, null, pageable);

        verify(bookRepository).filterAdmin(isNull(), isNull(), isNull(), isNull(),
                eq(seriesIds), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(pageable));
    }

    @Test
    void filter_nonAdminUsesLocationRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Long schoolId = 42L;
        when(sessionContext.hasRole(any())).thenReturn(false);
        when(locationRepository.findIdsBySchoolId(schoolId)).thenReturn(List.of(1L, 2L));
        when(bookRepository.filter(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        bookService.filter(schoolId, false, null, null, null, null, null, null, null,
                null, null, null, null, null, pageable);

        verify(locationRepository).findIdsBySchoolId(schoolId);
    }

    @Test
    void filter_nonAdminRequestsLocationOutsideSchool_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Long schoolId = 42L;
        when(locationRepository.findIdsBySchoolId(schoolId)).thenReturn(List.of(1L, 2L));

        Page<BookResultDTO> result = bookService.filter(schoolId, false, 99L, null,
                null, null, null, null, null, null, null, null, null, null, pageable);

        assertEquals(0, result.getTotalElements());
        verify(bookRepository, never()).filter(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void filter_blankQuery_treatedAsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        when(sessionContext.hasRole(any())).thenReturn(true);
        when(bookRepository.filterAdmin(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), isNull(), any()))
                .thenReturn(Page.empty());

        bookService.filter(null, true, null, null, null, null, null, null, null,
                null, null, null, null, " ", pageable);

        verify(bookRepository).filterAdmin(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), isNull(), eq(pageable));
    }

    @Test
    void filter_pagesMinGreaterThanPagesMax_throwsArgumentsInvalidException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(sessionContext.hasRole(any())).thenReturn(true);

        assertThrows(be.ap.backend.exception.ArgumentsInvalidException.class,
                () -> bookService.filter(null, true, null, null, null, null, null, null,
                        300, 100, null, null, null, null, pageable));
    }

    @Test
    void filter_nonLeerkracht_didacticForcedFalse() {
        Pageable pageable = PageRequest.of(0, 10);
        when(sessionContext.hasRole(any())).thenReturn(false);
        when(locationRepository.findIdsBySchoolId(any())).thenReturn(List.of(1L));
        when(bookRepository.filter(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), eq(Boolean.FALSE), any(), any()))
                .thenReturn(Page.empty());

        bookService.filter(1L, false, null, null, null, null, null, null, null, null,
                null, null, true, null, pageable);

        verify(bookRepository).filter(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), eq(Boolean.FALSE), any(), any());
    }

    @Test
    void filter_adminWithSpecificRequestedLocation_passesLocationList() {
        Pageable pageable = PageRequest.of(0, 10);
        when(sessionContext.hasRole(any())).thenReturn(true);
        when(bookRepository.filter(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        bookService.filter(null, true, 7L, null, null, null, null, null, null, null,
                null, null, null, null, pageable);

        // Admin + non-null requestedLocation → effectiveLocations = [7L], uses filter()
        // not filterAdmin()
        verify(bookRepository).filter(eq(List.of(7L)), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), eq(pageable));
        verify(bookRepository, never()).filterAdmin(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void filter_nonAdminWithValidRequestedLocation_usesOnlyThatLocation() {
        Pageable pageable = PageRequest.of(0, 10);
        Long schoolId = 10L;
        when(sessionContext.hasRole(any())).thenReturn(false);
        when(locationRepository.findIdsBySchoolId(schoolId)).thenReturn(List.of(1L, 2L, 3L));
        when(bookRepository.filter(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        bookService.filter(schoolId, false, 2L, null, null, null, null, null, null, null,
                null, null, null, null, pageable);

        // requestedLocation 2L is within school → effectiveLocations = [2L]
        verify(bookRepository).filter(eq(List.of(2L)), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), eq(pageable));
    }

    @Test
    void filter_emptyGenreIds_passesNullToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        when(sessionContext.hasRole(any())).thenReturn(true);
        when(bookRepository.filterAdmin(isNull(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        bookService.filter(null, true, null, List.of(), null, null, null, null, null,
                null, null, null, null, null, pageable);

        verify(bookRepository).filterAdmin(isNull(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), eq(pageable));
    }

    @Test
    void filter_emptyThemeIds_passesNullToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        when(sessionContext.hasRole(any())).thenReturn(true);
        when(bookRepository.filterAdmin(any(), any(), any(), any(), any(), any(),
                any(), any(), isNull(), any(), any(), any()))
                .thenReturn(Page.empty());

        bookService.filter(null, true, null, null, null, null, null, null, null,
                null, null, List.of(), null, null, pageable);

        verify(bookRepository).filterAdmin(any(), any(), any(), any(), any(), any(),
                any(), any(), isNull(), any(), any(), eq(pageable));
    }

    @Test
    void filter_pagesMinEqualsMax_doesNotThrow() {
        Pageable pageable = PageRequest.of(0, 10);
        when(sessionContext.hasRole(any())).thenReturn(true);
        when(bookRepository.filterAdmin(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        assertDoesNotThrow(() -> bookService.filter(null, true, null, null, null, null, null, null,
                200, 200, null, null, null, null, pageable));
    }

    // -------------------------------------------------------------------------
    // saveBook
    // -------------------------------------------------------------------------

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

        assertDoesNotThrow(() -> bookService.saveBook(dto));
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
        dto.setCoverUrl(" ");

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
    void saveBook_withSeriesCountNonZero_setsSeriesNumber() {
        CreateBookDTO dto = new CreateBookDTO();
        dto.setTitle("Part Two");
        dto.setBookType(1L);
        dto.setLanguage(2L);
        dto.setFiction(true);
        dto.setSeriesCount(2);

        when(entityManager.find(BookType.class, 1L)).thenReturn(new BookType());
        when(entityManager.find(Language.class, 2L)).thenReturn(new Language());
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.saveBook(dto);

        verify(bookRepository).save(argThat(b -> Integer.valueOf(2).equals(b.getSeriesNumber())));
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
    void saveBook_nullCoverUploadResult_doesNotSetCover() {
        CreateBookDTO dto = new CreateBookDTO();
        dto.setTitle("Ghost Book");
        dto.setBookType(1L);
        dto.setLanguage(2L);
        dto.setFiction(true);
        dto.setCoverUrl("https://example.com/missing.jpg");

        when(entityManager.find(BookType.class, 1L)).thenReturn(new BookType());
        when(entityManager.find(Language.class, 2L)).thenReturn(new Language());
        when(uploadService.saveCoverFromUrl(any())).thenReturn(null);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.saveBook(dto);

        verify(bookRepository).save(argThat(b -> b.getCover() == null));
    }

    @Test
    void saveBook_withNullCoverUrl_doesNotInvokeCoverUpload() {
        CreateBookDTO dto = new CreateBookDTO();
        dto.setTitle("No Cover Book");
        dto.setBookType(1L);
        dto.setLanguage(2L);
        dto.setFiction(true);
        dto.setCoverUrl(null);

        when(entityManager.find(BookType.class, 1L)).thenReturn(new BookType());
        when(entityManager.find(Language.class, 2L)).thenReturn(new Language());
        when(bookRepository.save(any(Book.class))).thenReturn(new Book());

        bookService.saveBook(dto);

        verify(uploadService, never()).saveCoverFromUrl(any());
    }

    @Test
    void saveBook_pagesZero_doesNotSetPages() {
        CreateBookDTO dto = new CreateBookDTO();
        dto.setTitle("Zero Pages");
        dto.setBookType(1L);
        dto.setLanguage(2L);
        dto.setFiction(true);
        dto.setPages(0);

        when(entityManager.find(BookType.class, 1L)).thenReturn(new BookType());
        when(entityManager.find(Language.class, 2L)).thenReturn(new Language());
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.saveBook(dto);

        verify(bookRepository).save(argThat(b -> b.getPages() == null));
    }

    @Test
    void saveBook_pagesNonZero_setsPages() {
        CreateBookDTO dto = new CreateBookDTO();
        dto.setTitle("Long Book");
        dto.setBookType(1L);
        dto.setLanguage(2L);
        dto.setFiction(true);
        dto.setPages(450);

        when(entityManager.find(BookType.class, 1L)).thenReturn(new BookType());
        when(entityManager.find(Language.class, 2L)).thenReturn(new Language());
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.saveBook(dto);

        verify(bookRepository).save(argThat(b -> Integer.valueOf(450).equals(b.getPages())));
    }

    @Test
    void saveBook_withAuthor_setsAuthor() {
        CreateBookDTO dto = new CreateBookDTO();
        dto.setTitle("Authored Book");
        dto.setBookType(1L);
        dto.setLanguage(2L);
        dto.setFiction(true);
        dto.setAuthor(7L);

        Author author = new Author();
        author.setName("Some Author");

        when(entityManager.find(BookType.class, 1L)).thenReturn(new BookType());
        when(entityManager.find(Language.class, 2L)).thenReturn(new Language());
        when(entityManager.find(Author.class, 7L)).thenReturn(author);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.saveBook(dto);

        verify(bookRepository).save(argThat(b -> author.equals(b.getAuthor())));
    }

    @Test
    void saveBook_withSeries_setsSeries() {
        CreateBookDTO dto = new CreateBookDTO();
        dto.setTitle("Series Book");
        dto.setBookType(1L);
        dto.setLanguage(2L);
        dto.setFiction(true);
        dto.setSeries(3L);

        Series series = new Series();
        series.setId(3L);

        when(entityManager.find(BookType.class, 1L)).thenReturn(new BookType());
        when(entityManager.find(Language.class, 2L)).thenReturn(new Language());
        when(entityManager.find(Series.class, 3L)).thenReturn(series);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.saveBook(dto);

        verify(bookRepository).save(argThat(b -> series.equals(b.getSeries())));
    }

    // -------------------------------------------------------------------------
    // updateBook
    // -------------------------------------------------------------------------

    @Test
    void updateBook_titleOnly_updatesTitle() {
        Book existingBook = existingBook();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateBookDTO dto = new UpdateBookDTO();
        dto.setTitle("New Title");

        bookService.updateBook(1L, dto);

        verify(bookRepository).save(argThat(b -> "New Title".equals(b.getTitle()) &&
                "1234567890".equals(b.getIsbn())));
    }

    @Test
    void updateBook_allSimpleFields_updatesAll() {
        Book existingBook = existingBook();
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

        bookService.updateBook(1L, dto);

        verify(bookRepository).save(argThat(b -> "Updated".equals(b.getTitle()) &&
                "9876543210".equals(b.getIsbn()) &&
                "New desc".equals(b.getDescription()) &&
                Boolean.FALSE.equals(b.getFiction()) &&
                Boolean.TRUE.equals(b.getDidactic()) &&
                Integer.valueOf(200).equals(b.getPages()) &&
                "cover.jpg".equals(b.getCover()) &&
                FontSize.GROOT.equals(b.getFontSize()) &&
                Clib.A.equals(b.getClib()) &&
                Integer.valueOf(3).equals(b.getSeriesNumber())));
    }

    @Test
    void updateBook_nullFields_doesNotOverwrite() {
        Book existingBook = existingBook();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        bookService.updateBook(1L, new UpdateBookDTO());

        verify(bookRepository).save(argThat(b -> "Original Title".equals(b.getTitle()) &&
                "1234567890".equals(b.getIsbn()) &&
                Integer.valueOf(100).equals(b.getPages())));
    }

    @Test
    void updateBook_withAuthor_setsAuthor() {
        Book existingBook = existingBook();
        Author author = new Author();
        author.setName("Test Author");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(entityManager.find(Author.class, 5L)).thenReturn(author);
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateBookDTO dto = new UpdateBookDTO();
        dto.setAuthor(5L);

        bookService.updateBook(1L, dto);

        verify(bookRepository).save(argThat(b -> author.equals(b.getAuthor())));
    }

    @Test
    void updateBook_withPublisher_setsPublisher() {
        Book existingBook = existingBook();
        Publisher publisher = new Publisher();
        publisher.setId(8L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(entityManager.find(Publisher.class, 8L)).thenReturn(publisher);
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateBookDTO dto = new UpdateBookDTO();
        dto.setPublisher(8L);

        bookService.updateBook(1L, dto);

        verify(bookRepository).save(argThat(b -> publisher.equals(b.getPublisher())));
    }

    @Test
    void updateBook_withLanguage_setsLanguage() {
        Book existingBook = existingBook();
        Language language = new Language();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(entityManager.find(Language.class, 3L)).thenReturn(language);
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateBookDTO dto = new UpdateBookDTO();
        dto.setLanguage(3L);

        bookService.updateBook(1L, dto);

        verify(bookRepository).save(argThat(b -> language.equals(b.getLanguage())));
    }

    @Test
    void updateBook_withBookType_setsBookType() {
        Book existingBook = existingBook();
        BookType bookType = new BookType();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(entityManager.find(BookType.class, 4L)).thenReturn(bookType);
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateBookDTO dto = new UpdateBookDTO();
        dto.setBookType(4L);

        bookService.updateBook(1L, dto);

        verify(bookRepository).save(argThat(b -> bookType.equals(b.getBookType())));
    }

    @Test
    void updateBook_withSeries_setsSeries() {
        Book existingBook = existingBook();
        Series series = new Series();
        series.setId(9L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(entityManager.find(Series.class, 9L)).thenReturn(series);
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateBookDTO dto = new UpdateBookDTO();
        dto.setSeries(9L);

        bookService.updateBook(1L, dto);

        verify(bookRepository).save(argThat(b -> series.equals(b.getSeries())));
    }

    @Test
    void updateBook_withGenres_setsGenres() {
        Book existingBook = existingBook();
        Genre g1 = new Genre();
        g1.setId(1L);
        Genre g2 = new Genre();
        g2.setId(2L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(entityManager.find(Genre.class, 1L)).thenReturn(g1);
        when(entityManager.find(Genre.class, 2L)).thenReturn(g2);
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateBookDTO dto = new UpdateBookDTO();
        dto.setGenres(Set.of(1L, 2L));

        bookService.updateBook(1L, dto);

        verify(bookRepository).save(argThat(b -> b.getGenres() != null &&
                b.getGenres().size() == 2 &&
                b.getGenres().containsAll(Set.of(g1, g2))));
    }

    @Test
    void updateBook_withThemes_setsThemes() {
        Book existingBook = existingBook();
        Theme t1 = new Theme();
        t1.setId(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(entityManager.find(Theme.class, 1L)).thenReturn(t1);
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateBookDTO dto = new UpdateBookDTO();
        dto.setThemes(Set.of(1L));

        bookService.updateBook(1L, dto);

        verify(bookRepository).save(argThat(b -> b.getThemes() != null && b.getThemes().size() == 1));
    }

    @Test
    void updateBook_bookNotFound_throwsEntityNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookService.updateBook(99L, new UpdateBookDTO()));
    }

    @Test
    void updateBook_saveIsCalledOnce() {
        Book existingBook = existingBook();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        bookService.updateBook(1L, new UpdateBookDTO());

        verify(bookRepository, times(1)).save(existingBook);
    }

    @Test
    void updateBook_withEmptyGenreSet_replacesGenresWithEmptySet() {
        Book existingBook = existingBook();
        existingBook.setGenres(Set.of(new Genre()));

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateBookDTO dto = new UpdateBookDTO();
        dto.setGenres(Set.of());

        bookService.updateBook(1L, dto);

        verify(bookRepository).save(argThat(b -> b.getGenres() != null && b.getGenres().isEmpty()));
    }

    @Test
    void updateBook_withEmptyThemeSet_replacesThemesWithEmptySet() {
        Book existingBook = existingBook();
        existingBook.setThemes(Set.of(new Theme()));

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateBookDTO dto = new UpdateBookDTO();
        dto.setThemes(Set.of());

        bookService.updateBook(1L, dto);

        verify(bookRepository).save(argThat(b -> b.getThemes() != null && b.getThemes().isEmpty()));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Book existingBook() {
        Book book = new Book();
        book.setTitle("Original Title");
        book.setIsbn("1234567890");
        book.setPages(100);
        book.setFiction(true);
        book.setDidactic(false);
        return book;
    }
}