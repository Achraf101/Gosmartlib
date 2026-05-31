package be.ap.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.BookCardDTO;
import be.ap.backend.dto.BookLookupDTO;
import be.ap.backend.dto.BookResultDTO;
import be.ap.backend.dto.CreateBookDTO;
import be.ap.backend.dto.UpdateBookDTO;
import be.ap.backend.entity.Author;
import be.ap.backend.entity.UserRole;
import be.ap.backend.enums.Clib;
import be.ap.backend.exception.ArgumentsInvalidException;
import be.ap.backend.service.BookService;
import be.ap.backend.service.IsbnLookupService;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class BookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookService bookService;

    @Mock
    private IsbnLookupService isbnLookupService;

    @Mock
    private SessionContext sessionContext;

    @InjectMocks
    private BookController controller;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Controller filter parameter order:
     * query, location, genres, language, fiction,
     * authorIds, seriesIds, pagesMin, pagesMax, clibs,
     * themes, didactic, page, size
     *
     * Service filter parameter order:
     * schoolId, isAdmin, location, genres, language, fiction,
     * authorIds, seriesIds, pagesMin, pagesMax, clibs,
     * themes, didactic, query, pageable
     */

    private BookResultDTO bookResultDTO(Long id, String title) {
        BookResultDTO dto = new BookResultDTO();
        dto.setId(id);
        dto.setTitle(title);
        return dto;
    }

    @BeforeEach
    void setUp() {
        lenient().when(sessionContext.hasRole(UserRole.ADMIN)).thenReturn(true);
        lenient().when(sessionContext.getRoles()).thenReturn(Set.of(UserRole.ADMIN));
        lenient().when(sessionContext.getSchoolId()).thenReturn(1L);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ------------------------------------------------------------------
    // addBook
    // ------------------------------------------------------------------

    @Test
    void givenValidDTO_whenAddBook_thenReturnSavedBookResultDTO() {
        CreateBookDTO input = new CreateBookDTO();
        input.setTitle("Test Book");

        BookResultDTO saved = bookResultDTO(1L, "Test Book");
        when(bookService.saveBook(input)).thenReturn(saved);

        BookResultDTO result = controller.addBook(input).getBody();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Book", result.getTitle());
        verify(bookService, times(1)).saveBook(input);
    }

    // ------------------------------------------------------------------
    // getAll
    // ------------------------------------------------------------------

    @Test
    void givenBooksExist_whenGetAll_thenReturnPagedBookResultDTOs() {
        Page<BookResultDTO> page = new PageImpl<>(List.of(
                bookResultDTO(1L, "Book 1"),
                bookResultDTO(2L, "Book 2")));

        when(bookService.getAll(isNull(), eq(false), any(Pageable.class))).thenReturn(page);

        Page<BookResultDTO> result = controller.getAll(false, null, 0, 5).getBody();

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("Book 1", result.getContent().get(0).getTitle());
        assertEquals("Book 2", result.getContent().get(1).getTitle());
        verify(bookService, times(1)).getAll(isNull(), eq(false), any(Pageable.class));
    }

    @Test
    void givenLocationParam_whenGetAll_thenForwardLocationToService() {
        Page<BookResultDTO> page = new PageImpl<>(List.of(bookResultDTO(1L, "Located Book")));
        when(bookService.getAll(eq(1L), eq(false), any(Pageable.class))).thenReturn(page);

        Page<BookResultDTO> result = controller.getAll(false, 1L, 0, 5).getBody();

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(bookService, times(1)).getAll(eq(1L), eq(false), any(Pageable.class));
    }

    // ------------------------------------------------------------------
    // getById
    // ------------------------------------------------------------------

    @Test
    void givenExistingId_whenGetById_thenReturnBookResultDTO() {
        BookResultDTO dto = bookResultDTO(1L, "Test Book");
        when(bookService.getById(1L)).thenReturn(dto);

        BookResultDTO result = controller.getById(1L).getBody();

        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
        verify(bookService, times(1)).getById(1L);
    }

    @Test
    void givenMissingId_whenGetById_thenThrowEntityNotFoundException() {
        when(bookService.getById(99L))
                .thenThrow(new EntityNotFoundException("Boek niet gevonden met id: 99"));

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> controller.getById(99L));

        assertEquals("Boek niet gevonden met id: 99", ex.getMessage());
        verify(bookService, times(1)).getById(99L);
    }

    // ------------------------------------------------------------------
    // search
    // ------------------------------------------------------------------

    @Test
    void givenSearchQuery_whenSearch_thenReturnMatchingBookResultDTOs() {
        Page<BookResultDTO> page = new PageImpl<>(List.of(bookResultDTO(1L, "Harry Potter")));
        when(bookService.search(eq("Harry"), any(Pageable.class))).thenReturn(page);

        Page<BookResultDTO> result = controller.search("Harry", 0, 5).getBody();

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Harry Potter", result.getContent().get(0).getTitle());
        verify(bookService, times(1)).search(eq("Harry"), any(Pageable.class));
    }

    // ------------------------------------------------------------------
    // getRelated
    // ------------------------------------------------------------------

    @Test
    void givenExistingBook_whenGetRelated_thenReturnRelatedBookCards() {
        Author author = new Author();
        author.setId(1L);
        author.setName("J.K. Rowling");

        List<BookCardDTO> related = List.of(
                new BookCardDTO(2L, "Title 1", "cover", author),
                new BookCardDTO(3L, "Title 2", "cover", author));

        when(bookService.getRelated(1L)).thenReturn(related);

        List<BookCardDTO> result = controller.getRelated(1L).getBody();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookService, times(1)).getRelated(1L);
    }

    // ------------------------------------------------------------------
    // getBooks (bookResult endpoint)
    // ------------------------------------------------------------------

    @Test
    void givenDefaultParams_whenGetBooks_thenReturnPage() {
        Page<BookResultDTO> mockPage = new PageImpl<>(List.of(mock(BookResultDTO.class)));
        when(bookService.getAllBookResults(PageRequest.of(0, 5))).thenReturn(mockPage);

        Page<BookResultDTO> result = controller.getBooks(0, 5).getBody();

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(bookService, times(1)).getAllBookResults(PageRequest.of(0, 5));
    }

    // ------------------------------------------------------------------
    // lookupByIsbn
    // ------------------------------------------------------------------

    @Test
    void givenValidIsbn_whenLookupByIsbn_thenReturn200WithBody() {
        BookLookupDTO dto = new BookLookupDTO();
        when(isbnLookupService.lookup("9780747532743")).thenReturn(Optional.of(dto));

        ResponseEntity<BookLookupDTO> response = controller.lookupByIsbn("9780747532743");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());
        verify(isbnLookupService, times(1)).lookup("9780747532743");
    }

    @Test
    void givenUnknownIsbn_whenLookupByIsbn_thenThrowEntityNotFoundException() {
        when(isbnLookupService.lookup("0000000000000")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> controller.lookupByIsbn("0000000000000"));

        verify(isbnLookupService, times(1)).lookup("0000000000000");
    }

    // ------------------------------------------------------------------
    // getIaPreview
    // ------------------------------------------------------------------

    @Test
    void givenBookWithIsbnAndIaFound_whenGetIaPreview_thenReturn200WithIaId() {
        when(bookService.getIaPreview(1L))
                .thenReturn(ResponseEntity.ok(Map.of("ia_id", "lordofrings00tolk_5")));

        ResponseEntity<Map<String, String>> response = controller.getIaPreview(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("lordofrings00tolk_5", response.getBody().get("ia_id"));
    }

    @Test
    void givenBookWithIsbnButNoIaFound_whenGetIaPreview_thenReturn404() {
        when(bookService.getIaPreview(2L)).thenReturn(ResponseEntity.notFound().build());

        assertEquals(404, controller.getIaPreview(2L).getStatusCode().value());
    }

    @Test
    void givenBookWithoutIsbn_whenGetIaPreview_thenReturn404() {
        when(bookService.getIaPreview(3L)).thenReturn(ResponseEntity.notFound().build());

        assertEquals(404, controller.getIaPreview(3L).getStatusCode().value());
    }

    @Test
    void givenMissingBook_whenGetIaPreview_thenReturn404() {
        when(bookService.getIaPreview(99L)).thenReturn(ResponseEntity.notFound().build());

        assertEquals(404, controller.getIaPreview(99L).getStatusCode().value());
    }

    // ------------------------------------------------------------------
    // filter — ADMIN session (schoolId=null, isAdmin=true)
    //
    // Controller signature:
    // filter(query, location, genres, language, fiction,
    // authorIds, seriesIds, pagesMin, pagesMax, clibs,
    // themes, didactic, page, size)
    //
    // Service signature:
    // filter(schoolId, isAdmin, location, genres, language, fiction,
    // authorIds, seriesIds, pagesMin, pagesMax, clibs,
    // themes, didactic, query, pageable)
    // ------------------------------------------------------------------

    @Test
    void givenNoParams_whenFilter_thenReturnAllBooks() {
        Page<BookResultDTO> mockPage = new PageImpl<>(List.of(
                bookResultDTO(1L, "A"), bookResultDTO(2L, "B"), bookResultDTO(3L, "C")));

        when(bookService.filter(
                isNull(), eq(true), // schoolId, isAdmin
                isNull(), isNull(), isNull(), isNull(), // location, genres, language, fiction
                isNull(), isNull(), // authorIds, seriesIds
                isNull(), isNull(), // pagesMin, pagesMax
                isNull(), isNull(), isNull(), // clibs, themes, didactic
                isNull(), // query
                any(Pageable.class)))
                .thenReturn(mockPage);

        // controller: filter(query=null, location=null, genres=null, language=null,
        // fiction=null, authorIds=null, seriesIds=null,
        // pagesMin=null, pagesMax=null, clibs=null,
        // themes=null, didactic=null, page=0, size=5)
        Page<BookResultDTO> result = controller.filter(
                null, null, null, null,
                null, null, null,
                null, null, null,
                null, null, 0, 5).getBody();

        assertEquals(3, result.getTotalElements());
    }

    @Test
    void givenFictionTrue_whenFilter_thenReturnOnlyFictionBooks() {
        BookResultDTO dto = bookResultDTO(1L, "De brief voor de koning");
        dto.setFiction(true);
        Page<BookResultDTO> mockPage = new PageImpl<>(List.of(dto));

        when(bookService.filter(
                isNull(), eq(true),
                isNull(), isNull(), isNull(), eq(true),
                isNull(), isNull(),
                isNull(), isNull(),
                isNull(), isNull(), isNull(),
                isNull(),
                any(Pageable.class)))
                .thenReturn(mockPage);

        // controller: fiction is the 5th param
        Page<BookResultDTO> result = controller.filter(
                null, null, null, null,
                true, null, null,
                null, null, null,
                null, null, 0, 5).getBody();

        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).isFiction());
    }

    @Test
    void givenGenreFilter_whenFilter_thenReturnMatchingBooks() {
        Page<BookResultDTO> mockPage = new PageImpl<>(
                List.of(bookResultDTO(1L, "Harry Potter en de vuurbeker")));

        when(bookService.filter(
                isNull(), eq(true),
                isNull(), eq(List.of(1L)), isNull(), isNull(),
                isNull(), isNull(),
                isNull(), isNull(),
                isNull(), isNull(), isNull(),
                isNull(),
                any(Pageable.class)))
                .thenReturn(mockPage);

        // controller: genres is the 3rd param
        Page<BookResultDTO> result = controller.filter(
                null, null, List.of(1L), null,
                null, null, null,
                null, null, null,
                null, null, 0, 5).getBody();

        assertEquals(1, result.getTotalElements());
        assertEquals("Harry Potter en de vuurbeker", result.getContent().get(0).getTitle());
    }

    @Test
    void givenAuthorFilter_whenFilter_thenReturnMatchingBooks() {
        Page<BookResultDTO> mockPage = new PageImpl<>(
                List.of(bookResultDTO(1L, "Kruistocht in Spijkerbroek")));

        when(bookService.filter(
                isNull(), eq(true),
                isNull(), isNull(), isNull(), isNull(),
                eq(List.of(2L)), isNull(),
                isNull(), isNull(),
                isNull(), isNull(), isNull(),
                isNull(),
                any(Pageable.class)))
                .thenReturn(mockPage);

        // controller: authorIds is the 6th param
        Page<BookResultDTO> result = controller.filter(
                null, null, null, null,
                null, List.of(2L), null,
                null, null, null,
                null, null, 0, 5).getBody();

        assertEquals(1, result.getTotalElements());
        assertEquals("Kruistocht in Spijkerbroek", result.getContent().get(0).getTitle());
    }

    @Test
    void givenSeriesFilter_whenFilter_thenReturnMatchingBooks() {
        Page<BookResultDTO> mockPage = new PageImpl<>(
                List.of(bookResultDTO(1L, "Harry Potter en de Steen der Wijzen")));

        when(bookService.filter(
                isNull(), eq(true),
                isNull(), isNull(), isNull(), isNull(),
                isNull(), eq(List.of(1L)),
                isNull(), isNull(),
                isNull(), isNull(), isNull(),
                isNull(),
                any(Pageable.class)))
                .thenReturn(mockPage);

        // controller: seriesIds is the 7th param
        Page<BookResultDTO> result = controller.filter(
                null, null, null, null,
                null, null, List.of(1L),
                null, null, null,
                null, null, 0, 5).getBody();

        assertEquals(1, result.getTotalElements());
        assertEquals("Harry Potter en de Steen der Wijzen",
                result.getContent().get(0).getTitle());
    }

    @Test
    void givenThemeFilter_whenFilter_thenReturnMatchingBooks() {
        Page<BookResultDTO> mockPage = new PageImpl<>(
                List.of(bookResultDTO(1L, "De Alchemist")));

        when(bookService.filter(
                isNull(), eq(true),
                isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(),
                isNull(), isNull(),
                isNull(), eq(List.of(5L)), isNull(),
                isNull(),
                any(Pageable.class)))
                .thenReturn(mockPage);

        // controller: themes is the 11th param
        Page<BookResultDTO> result = controller.filter(
                null, null, null, null,
                null, null, null,
                null, null, null,
                List.of(5L), null, 0, 5).getBody();

        assertEquals(1, result.getTotalElements());
        assertEquals("De Alchemist", result.getContent().get(0).getTitle());
    }

    @Test
    void givenPageRangeFilter_whenFilter_thenReturnMatchingBooks() {
        Page<BookResultDTO> mockPage = new PageImpl<>(
                List.of(bookResultDTO(1L, "De brief voor de koning")));

        when(bookService.filter(
                isNull(), eq(true),
                isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(),
                eq(100), eq(500),
                isNull(), isNull(), isNull(),
                isNull(),
                any(Pageable.class)))
                .thenReturn(mockPage);

        // controller: pagesMin=8th, pagesMax=9th
        Page<BookResultDTO> result = controller.filter(
                null, null, null, null,
                null, null, null,
                100, 500, null,
                null, null, 0, 5).getBody();

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void givenClibFilter_whenFilter_thenReturnMatchingBooks() {
        List<Clib> clibs = List.of(Clib.A);
        Page<BookResultDTO> mockPage = new PageImpl<>(List.of(bookResultDTO(1L, "Clib Book")));

        when(bookService.filter(
                isNull(), eq(true),
                isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(),
                isNull(), isNull(),
                eq(clibs), isNull(), isNull(),
                isNull(),
                any(Pageable.class)))
                .thenReturn(mockPage);

        // controller: clibs is the 10th param
        Page<BookResultDTO> result = controller.filter(
                null, null, null, null,
                null, null, null,
                null, null, clibs,
                null, null, 0, 5).getBody();

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void givenPagesMinGreaterThanMax_whenFilter_thenThrowArgumentsInvalidException() {
        when(bookService.filter(
                isNull(), eq(true),
                isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(),
                eq(500), eq(100),
                isNull(), isNull(), isNull(),
                isNull(),
                any(Pageable.class)))
                .thenThrow(new ArgumentsInvalidException("pagesMin moet kleiner zijn dan pagesMax"));

        assertThrows(ArgumentsInvalidException.class,
                () -> controller.filter(
                        null, null, null, null,
                        null, null, null,
                        500, 100, null,
                        null, null, 0, 5));
    }

    @Test
    void givenPagesMinEqualToMax_whenFilter_thenProceedNormally() {
        Page<BookResultDTO> mockPage = new PageImpl<>(List.of(bookResultDTO(1L, "Equal Pages Book")));

        when(bookService.filter(
                isNull(), eq(true),
                isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(),
                eq(200), eq(200),
                isNull(), isNull(), isNull(),
                isNull(),
                any(Pageable.class)))
                .thenReturn(mockPage);

        Page<BookResultDTO> result = controller.filter(
                null, null, null, null,
                null, null, null,
                200, 200, null,
                null, null, 0, 5).getBody();

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void givenLocationParam_whenFilter_thenForwardLocationToService() {
        Page<BookResultDTO> mockPage = new PageImpl<>(List.of(bookResultDTO(1L, "Located Book")));

        when(bookService.filter(
                isNull(), eq(true),
                eq(1L), isNull(), isNull(), isNull(),
                isNull(), isNull(),
                isNull(), isNull(),
                isNull(), isNull(), isNull(),
                isNull(),
                any(Pageable.class)))
                .thenReturn(mockPage);

        // controller: location is the 2nd param
        Page<BookResultDTO> result = controller.filter(
                null, 1L, null, null,
                null, null, null,
                null, null, null,
                null, null, 0, 5).getBody();

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void givenQueryParam_whenFilter_thenForwardQueryToService() {
        Page<BookResultDTO> mockPage = new PageImpl<>(List.of(bookResultDTO(1L, "Matching Book")));

        when(bookService.filter(
                isNull(), eq(true),
                isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(),
                isNull(), isNull(),
                isNull(), isNull(), isNull(),
                eq("hobbit"),
                any(Pageable.class)))
                .thenReturn(mockPage);

        // controller: query is the 1st param
        Page<BookResultDTO> result = controller.filter(
                "hobbit", null, null, null,
                null, null, null,
                null, null, null,
                null, null, 0, 5).getBody();

        assertEquals(1, result.getTotalElements());
    }

    // ------------------------------------------------------------------
    // filter — non-ADMIN session
    // ------------------------------------------------------------------

    @Test
    void givenNonAdminUser_whenFilter_thenSchoolIdPassedToService() {
        when(sessionContext.hasRole(UserRole.ADMIN)).thenReturn(false);
        when(sessionContext.getSchoolId()).thenReturn(42L);

        Page<BookResultDTO> mockPage = new PageImpl<>(List.of(bookResultDTO(1L, "School Book")));

        when(bookService.filter(
                eq(42L), eq(false),
                isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(),
                isNull(), isNull(),
                isNull(), isNull(), isNull(),
                isNull(),
                any(Pageable.class)))
                .thenReturn(mockPage);

        Page<BookResultDTO> result = controller.filter(
                null, null, null, null,
                null, null, null,
                null, null, null,
                null, null, 0, 5).getBody();

        assertEquals(1, result.getTotalElements());
        verify(bookService, times(1)).filter(
                eq(42L), eq(false),
                isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(),
                isNull(), isNull(),
                isNull(), isNull(), isNull(),
                isNull(),
                any(Pageable.class));
    }

    // ------------------------------------------------------------------
    // updateBook (MockMvc)
    // ------------------------------------------------------------------

    @Test
    void updateBook_validRequest_returns200WithUpdatedTitle() throws Exception {
        BookResultDTO updated = bookResultDTO(1L, "Updated Title");
        when(bookService.updateBook(eq(1L), any(UpdateBookDTO.class))).thenReturn(updated);

        UpdateBookDTO dto = new UpdateBookDTO();
        dto.setTitle("Updated Title");

        mockMvc.perform(put("/book/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void updateBook_bookNotFound_returns404() throws Exception {
        when(bookService.updateBook(eq(99L), any(UpdateBookDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(put("/book/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateBookDTO())))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBook_emptyBody_returns200() throws Exception {
        when(bookService.updateBook(eq(1L), any(UpdateBookDTO.class)))
                .thenReturn(new BookResultDTO());

        mockMvc.perform(put("/book/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateBook_serviceCalledWithCorrectId() throws Exception {
        when(bookService.updateBook(eq(1L), any(UpdateBookDTO.class)))
                .thenReturn(new BookResultDTO());

        mockMvc.perform(put("/book/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

        verify(bookService, times(1)).updateBook(eq(1L), any(UpdateBookDTO.class));
    }
}