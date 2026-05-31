package be.ap.backend.controller;

import be.ap.backend.dto.SectionBookDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Section;
import be.ap.backend.service.SectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SectionController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = SectionController.class)
public class SectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SectionService sectionService;

    // -------------------------------------------------------------------------
    // GET /section?schoolId={id}
    // -------------------------------------------------------------------------

    @Test
    void getAllSections_returnsSections() throws Exception {
        Section section = new Section();
        section.setId(1L);
        when(sectionService.getAllSections(1L)).thenReturn(List.of(section));

        mockMvc.perform(get("/section").param("schoolId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(sectionService, times(1)).getAllSections(1L);
    }

    @Test
    void getAllSections_returnsEmptyList_whenNoSections() throws Exception {
        when(sectionService.getAllSections(99L)).thenReturn(List.of());

        mockMvc.perform(get("/section").param("schoolId", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // -------------------------------------------------------------------------
    // GET /section/{id}/books
    // -------------------------------------------------------------------------

    @Test
    void getBooksBySection_returnsBooks() throws Exception {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Harry Potter en de vuurbeker");
        when(sectionService.getBooksBySection(1L)).thenReturn(List.of(book));

        mockMvc.perform(get("/section/1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Harry Potter en de vuurbeker"));

        verify(sectionService, times(1)).getBooksBySection(1L);
    }

    @Test
    void getBooksBySection_returnsEmptyList_whenNoBooks() throws Exception {
        when(sectionService.getBooksBySection(1L)).thenReturn(List.of());

        mockMvc.perform(get("/section/1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // -------------------------------------------------------------------------
    // GET /section/{id}/books/grade?grade={grade}
    // -------------------------------------------------------------------------

    @Test
    void getBookBySectionAndGrade_returnsBook() throws Exception {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("De brief voor de koning");
        when(sectionService.getBookBySectionAndGrade(1L, (byte) 1)).thenReturn(book);

        mockMvc.perform(get("/section/1/books/grade").param("grade", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("De brief voor de koning"));

        verify(sectionService, times(1)).getBookBySectionAndGrade(1L, (byte) 1);
    }

    @Test
    void getBookBySectionAndGrade_returnsNull_whenNoBook() throws Exception {
        when(sectionService.getBookBySectionAndGrade(1L, (byte) 2)).thenReturn(null);

        mockMvc.perform(get("/section/1/books/grade").param("grade", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());

        verify(sectionService, times(1)).getBookBySectionAndGrade(1L, (byte) 2);
    }

    // -------------------------------------------------------------------------
    // PUT /section/{id}/book?bookId={bookId}&grade={grade}
    // -------------------------------------------------------------------------

    @Test
    void setBookOfMonth_returnsBook() throws Exception {
        Book book = new Book();
        book.setId(2L);
        book.setTitle("Harry Potter en de vuurbeker");
        when(sectionService.setBookOfMonth(1L, 2L, (byte) 1)).thenReturn(book);

        mockMvc.perform(put("/section/1/book")
                .param("bookId", "2")
                .param("grade", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Harry Potter en de vuurbeker"));

        verify(sectionService, times(1)).setBookOfMonth(1L, 2L, (byte) 1);
    }

    @Test
    void setBookOfMonth_returnsNull_whenServiceReturnsNull() throws Exception {
        when(sectionService.setBookOfMonth(1L, 99L, (byte) 1)).thenReturn(null);

        mockMvc.perform(put("/section/1/book")
                .param("bookId", "99")
                .param("grade", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }

    // -------------------------------------------------------------------------
    // PUT /section/{sectionId}/spotlight?bookId={bookId}&ranking={ranking}
    // -------------------------------------------------------------------------

    @Test
    void setSpotlightBook_returnsBook() throws Exception {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("De brief voor de koning");
        when(sectionService.setSpotlightBook(eq(1L), eq(1L), eq((short) 1))).thenReturn(book);

        mockMvc.perform(put("/section/1/spotlight")
                .param("bookId", "1")
                .param("ranking", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("De brief voor de koning"));

        verify(sectionService, times(1)).setSpotlightBook(1L, 1L, (short) 1);
    }

    @Test
    void setSpotlightBook_returnsNull_whenServiceReturnsNull() throws Exception {
        when(sectionService.setSpotlightBook(eq(1L), eq(99L), eq((short) 1))).thenReturn(null);

        mockMvc.perform(put("/section/1/spotlight")
                .param("bookId", "99")
                .param("ranking", "1"))
                .andExpect(status().isOk());

        verify(sectionService, times(1)).setSpotlightBook(1L, 99L, (short) 1);
    }

    // -------------------------------------------------------------------------
    // GET /section/{id}/spotlight
    // -------------------------------------------------------------------------

    @Test
    void getSpotlightBooks_returnsListOfDTOs() throws Exception {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("De brief voor de koning");
        SectionBookDTO dto = new SectionBookDTO((short) 1, book);
        when(sectionService.getSpotlightBooks(eq(1L))).thenReturn(List.of(dto));

        mockMvc.perform(get("/section/1/spotlight"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ranking").value(1))
                .andExpect(jsonPath("$[0].book.title").value("De brief voor de koning"));

        verify(sectionService, times(1)).getSpotlightBooks(1L);
    }

    @Test
    void getSpotlightBooks_returnsEmptyList_whenNoSpotlightBooks() throws Exception {
        when(sectionService.getSpotlightBooks(eq(1L))).thenReturn(List.of());

        mockMvc.perform(get("/section/1/spotlight"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}