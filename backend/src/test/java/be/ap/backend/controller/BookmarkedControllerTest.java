package be.ap.backend.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import be.ap.backend.dto.BookmarkedDTO;
import be.ap.backend.entity.Author;
import be.ap.backend.service.BookmarkedService;

@WebMvcTest(BookmarkedController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BookmarkedControllerTest {

    @MockitoBean
    private BookmarkedService bookmarkedService;

    @Autowired
    private MockMvc mockMvc;

    // ── helpers ───────────────────────────────────────────────────────────────

    private Author buildAuthor(Long id, String name, String description) {
        Author author = new Author();
        author.setId(id);
        author.setName(name);
        author.setDescription(description);
        return author;
    }

    // ── GET /bookmarked/{userId} ───────────────────────────────────────────────

    @Test
    void getBookmarkeds_returnsListOfDTOs() throws Exception {
        Author author = buildAuthor(1L, "J.K. Rowling", "British author");
        BookmarkedDTO dto = new BookmarkedDTO(1L, 1L, "Harry Potter", "cover.webp", author, null);
        when(bookmarkedService.getBookmarked(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/bookmarked/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Harry Potter"))
                .andExpect(jsonPath("$[0].cover").value("cover.webp"))
                .andExpect(jsonPath("$[0].author.name").value("J.K. Rowling"));

        verify(bookmarkedService, times(1)).getBookmarked(1L);
    }

    @Test
    void getBookmarkeds_multipleItems_returnsAll() throws Exception {
        Author author = buildAuthor(1L, "J.K. Rowling", "British author");
        BookmarkedDTO dto1 = new BookmarkedDTO(1L, 1L, "Harry Potter", "cover1.webp", author, null);
        BookmarkedDTO dto2 = new BookmarkedDTO(2L, 2L, "Fantastic Beasts", "cover2.webp", author, null);
        when(bookmarkedService.getBookmarked(1L)).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/bookmarked/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Harry Potter"))
                .andExpect(jsonPath("$[1].title").value("Fantastic Beasts"));
    }

    @Test
    void getBookmarkeds_emptyList_returnsEmpty() throws Exception {
        when(bookmarkedService.getBookmarked(99L)).thenReturn(List.of());

        mockMvc.perform(get("/bookmarked/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getBookmarkeds_delegatesToServiceWithCorrectUserId() throws Exception {
        when(bookmarkedService.getBookmarked(42L)).thenReturn(List.of());

        mockMvc.perform(get("/bookmarked/42"))
                .andExpect(status().isOk());

        verify(bookmarkedService, times(1)).getBookmarked(42L);
        verify(bookmarkedService, never()).getBookmarked(argThat(id -> !id.equals(42L)));
    }

    // ── GET /bookmarked/{userId}/{bookId} ─────────────────────────────────────

    @Test
    void isBookmarked_returnsTrue_whenBookmarked() throws Exception {
        when(bookmarkedService.isBookmarked(1L, 1L)).thenReturn(true);

        mockMvc.perform(get("/bookmarked/1/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(bookmarkedService, times(1)).isBookmarked(1L, 1L);
    }

    @Test
    void isBookmarked_returnsFalse_whenNotBookmarked() throws Exception {
        when(bookmarkedService.isBookmarked(1L, 99L)).thenReturn(false);

        mockMvc.perform(get("/bookmarked/1/99"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(bookmarkedService, times(1)).isBookmarked(1L, 99L);
    }

    @Test
    void isBookmarked_delegatesCorrectUserAndBookId() throws Exception {
        when(bookmarkedService.isBookmarked(7L, 13L)).thenReturn(true);

        mockMvc.perform(get("/bookmarked/7/13"))
                .andExpect(status().isOk());

        verify(bookmarkedService).isBookmarked(7L, 13L);
        verify(bookmarkedService, never()).isBookmarked(eq(7L), argThat(id -> !id.equals(13L)));
    }

    // ── POST /bookmarked/{userId}/{bookId} ────────────────────────────────────

    @Test
    void toggleBookmarked_returnsTrue_whenBookmarkAdded() throws Exception {
        when(bookmarkedService.toggleBookmarked(1L, 1L)).thenReturn(true);

        mockMvc.perform(post("/bookmarked/1/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(bookmarkedService, times(1)).toggleBookmarked(1L, 1L);
    }

    @Test
    void toggleBookmarked_returnsFalse_whenBookmarkRemoved() throws Exception {
        when(bookmarkedService.toggleBookmarked(1L, 1L)).thenReturn(false);

        mockMvc.perform(post("/bookmarked/1/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(bookmarkedService, times(1)).toggleBookmarked(1L, 1L);
    }

    @Test
    void toggleBookmarked_calledOncePerRequest() throws Exception {
        when(bookmarkedService.toggleBookmarked(2L, 5L)).thenReturn(true);

        mockMvc.perform(post("/bookmarked/2/5"))
                .andExpect(status().isOk());

        verify(bookmarkedService, times(1)).toggleBookmarked(2L, 5L);
        verifyNoMoreInteractions(bookmarkedService);
    }

    @Test
    void toggleBookmarked_delegatesCorrectUserAndBookId() throws Exception {
        when(bookmarkedService.toggleBookmarked(3L, 8L)).thenReturn(false);

        mockMvc.perform(post("/bookmarked/3/8"))
                .andExpect(status().isOk());

        verify(bookmarkedService).toggleBookmarked(3L, 8L);
        verify(bookmarkedService, never()).toggleBookmarked(argThat(id -> !id.equals(3L)), any());
    }
}