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

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.SharedListResponseDTO;
import be.ap.backend.entity.BookList;
import be.ap.backend.entity.SavedList;
import be.ap.backend.service.SavedListService;

@WebMvcTest(controllers = SavedListController.class)
@AutoConfigureMockMvc(addFilters = false) // skip Spring Security filter chain
public class SavedListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SavedListService savedListService;

    @MockitoBean
    private SessionContext sessionContext;

    // ── GET /saved-lists ───────────────────────────────────────────────────────

    @Test
    void givenLoggedInUser_whenGetSavedLists_thenReturnSavedLists() throws Exception {
        when(sessionContext.getUserId()).thenReturn(1L);

        BookList bookList = new BookList();
        bookList.setId(1L);
        bookList.setName("Shared List");
        SharedListResponseDTO dto = new SharedListResponseDTO(bookList, List.of());
        when(savedListService.getSavedLists(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/saved-lists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(savedListService).getSavedLists(1L);
    }

    @Test
    void givenLoggedInUser_whenGetSavedLists_thenReturnEmptyList_whenNone() throws Exception {
        when(sessionContext.getUserId()).thenReturn(1L);
        when(savedListService.getSavedLists(1L)).thenReturn(List.of());

        mockMvc.perform(get("/saved-lists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(savedListService).getSavedLists(1L);
    }

    @Test
    void givenNoSession_whenGetSavedLists_thenThrowMissingSessionException() throws Exception {
        when(sessionContext.getUserId()).thenReturn(null);

        mockMvc.perform(get("/saved-lists"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(savedListService);
    }

    // ── POST /saved-lists/{token} ──────────────────────────────────────────────

    @Test
    void givenLoggedInUserAndToken_whenSaveList_thenReturnSavedList() throws Exception {
        when(sessionContext.getUserId()).thenReturn(1L);

        BookList bookList = new BookList();
        bookList.setId(1L);
        SavedList savedList = new SavedList();
        savedList.setUserId(1L);
        savedList.setBookListId(1L);

        when(savedListService.getListByToken("abc123")).thenReturn(bookList);
        when(savedListService.saveList(1L, 1L)).thenReturn(savedList);

        mockMvc.perform(post("/saved-lists/abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.bookListId").value(1));

        verify(savedListService).getListByToken("abc123");
        verify(savedListService).saveList(1L, 1L);
    }

    @Test
    void givenNoSession_whenSaveList_thenThrowMissingSessionException() throws Exception {
        when(sessionContext.getUserId()).thenReturn(null);

        mockMvc.perform(post("/saved-lists/abc123"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(savedListService);
    }

    // ── DELETE /saved-lists/{bookListId} ──────────────────────────────────────

    @Test
    void givenLoggedInUser_whenUnsaveList_thenReturn204() throws Exception {
        when(sessionContext.getUserId()).thenReturn(1L);
        doNothing().when(savedListService).unsaveList(1L, 1L);

        mockMvc.perform(delete("/saved-lists/1"))
                .andExpect(status().isNoContent());

        verify(savedListService).unsaveList(1L, 1L);
    }

    @Test
    void givenNoSession_whenUnsaveList_thenThrowMissingSessionException() throws Exception {
        when(sessionContext.getUserId()).thenReturn(null);

        mockMvc.perform(delete("/saved-lists/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(savedListService);
    }

    // ── GET /saved-lists/{bookListId}/exists ──────────────────────────────────

    @Test
    void givenLoggedInUser_whenIsSaved_thenReturnTrue() throws Exception {
        when(sessionContext.getUserId()).thenReturn(1L);
        when(savedListService.isSaved(1L, 1L)).thenReturn(true);

        mockMvc.perform(get("/saved-lists/1/exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(savedListService).isSaved(1L, 1L);
    }

    @Test
    void givenLoggedInUser_whenIsSaved_thenReturnFalse_whenNotSaved() throws Exception {
        when(sessionContext.getUserId()).thenReturn(1L);
        when(savedListService.isSaved(1L, 99L)).thenReturn(false);

        mockMvc.perform(get("/saved-lists/99/exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(savedListService).isSaved(1L, 99L);
    }

    @Test
    void givenNoSession_whenIsSaved_thenThrowMissingSessionException() throws Exception {
        when(sessionContext.getUserId()).thenReturn(null);

        mockMvc.perform(get("/saved-lists/1/exists"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(savedListService);
    }
}