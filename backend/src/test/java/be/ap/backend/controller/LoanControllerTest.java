package be.ap.backend.controller;

import be.ap.backend.dto.LoanDTO;
import be.ap.backend.dto.UpdateNoteDTO;
import be.ap.backend.dto.UpdateStatusDTO;
import be.ap.backend.entity.LoanStatus;
import be.ap.backend.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LoanController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = LoanController.class)
public class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoanService loanService;

    // ── GET /loan/requested ───────────────────────────────────────

    @Test
    void getRequested_returnsListOfLoans() throws Exception {
        LoanDTO dto = new LoanDTO();
        when(loanService.getRequested()).thenReturn(List.of(dto));

        mockMvc.perform(get("/loan/requested"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getRequested_empty_returnsEmptyList() throws Exception {
        when(loanService.getRequested()).thenReturn(List.of());

        mockMvc.perform(get("/loan/requested"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── POST /loan ────────────────────────────────────────────────

    @Test
    void createLoan_success() throws Exception {
        LoanDTO dto = new LoanDTO();
        when(loanService.createLoan(any())).thenReturn(List.of(dto));

        mockMvc.perform(post("/loan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void createLoan_serviceThrowsIllegalArgument_propagatesException() throws Exception {
        LoanDTO dto = new LoanDTO();
        when(loanService.createLoan(any()))
                .thenThrow(new IllegalArgumentException("minstens 1 boek is verplicht"));

        assertThatThrownBy(() -> mockMvc.perform(post("/loan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))))
                .cause()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minstens 1 boek is verplicht");
    }

    @Test
    void createLoan_serviceThrowsEntityNotFound_propagatesException() throws Exception {
        LoanDTO dto = new LoanDTO();
        when(loanService.createLoan(any()))
                .thenThrow(new EntityNotFoundException("Gebruiker niet gevonden"));

        assertThatThrownBy(() -> mockMvc.perform(post("/loan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))))
                .cause()
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Gebruiker niet gevonden");
    }

    // ── PUT /loan/{id}/note ───────────────────────────────────────

    @Test
    void updateNote_success() throws Exception {
        LoanDTO result = new LoanDTO();
        UpdateNoteDTO updateNoteDTO = new UpdateNoteDTO("Opmerking");
        when(loanService.updateNote(eq(1L), eq("Opmerking"))).thenReturn(result);

        mockMvc.perform(put("/loan/1/note")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateNoteDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void updateNote_serviceThrowsEntityNotFound_propagatesException() throws Exception {
        UpdateNoteDTO updateNoteDTO = new UpdateNoteDTO("Opmerking");
        when(loanService.updateNote(eq(99L), any()))
                .thenThrow(new EntityNotFoundException("Loan niet gevonden met id: 99"));

        assertThatThrownBy(() -> mockMvc.perform(put("/loan/99/note")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateNoteDTO))))
                .cause()
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Loan niet gevonden met id: 99");
    }
    // ── PUT /loan/{id}/status ─────────────────────────────────────

    @Test
    void updateStatus_success() throws Exception {
        LoanDTO result = new LoanDTO();
        UpdateStatusDTO updateStatusDTO = new UpdateStatusDTO(LoanStatus.ACCEPTED);
        when(loanService.updateStatus(eq(1L), eq(LoanStatus.ACCEPTED))).thenReturn(result);

        mockMvc.perform(put("/loan/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateStatusDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatus_serviceThrowsEntityNotFound_propagatesException() throws Exception {
        UpdateStatusDTO updateStatusDTO = new UpdateStatusDTO(LoanStatus.ACCEPTED);
        when(loanService.updateStatus(eq(99L), any()))
                .thenThrow(new EntityNotFoundException("Loan niet gevonden met id: 99"));

        assertThatThrownBy(() -> mockMvc.perform(put("/loan/99/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateStatusDTO))))
                .cause()
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Loan niet gevonden met id: 99");
    }

    // ── GET /loan/user ────────────────────────────────────────────

    @Test
    void getByUserId_withSessionUserId_returnsLoans() throws Exception {
        LoanDTO dto = new LoanDTO();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);
        when(loanService.getByUserId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/loan/user").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByUserId_withStringSessionUserId_returnsLoans() throws Exception {
        // HttpSession attributes may be stored as String depending on the session
        // provider.
        LoanDTO dto = new LoanDTO();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "2");
        when(loanService.getByUserId(2L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/loan/user").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByUserId_noSessionAttribute_passesNullToService() throws Exception {
        when(loanService.getByUserId(null)).thenReturn(List.of());

        mockMvc.perform(get("/loan/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(loanService).getByUserId(null);
    }

    // ── DELETE /loan/{id} ─────────────────────────────────────────

    @Test
    void deleteLoan_success_returns204() throws Exception {
        doNothing().when(loanService).deleteLoan(1L);

        mockMvc.perform(delete("/loan/1"))
                .andExpect(status().isNoContent());

        verify(loanService).deleteLoan(1L);
    }

    @Test
    void deleteLoan_serviceThrowsRuntimeException_propagatesException() throws Exception {
        doThrow(new RuntimeException("Uitlening niet gevonden!"))
                .when(loanService).deleteLoan(99L);

        assertThatThrownBy(() -> mockMvc.perform(delete("/loan/99")))
                .cause()
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Uitlening niet gevonden");
    }

    @Test
    void getOverdueLoans_returnsOk() throws Exception {
        when(loanService.getOverdueLoans(eq(1L))).thenReturn(List.of());

        mockMvc.perform(get("/loan/overdue")
                .sessionAttr("campus", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void getOverdueLoansLength_returnsCount() throws Exception {
        when(loanService.getOverdueLoansLength(eq(1L))).thenReturn(3);

        mockMvc.perform(get("/loan/overdue/length")
                .sessionAttr("campus", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    void getDueSoonLoans_returnsOk() throws Exception {
        when(loanService.getDueSoonLoans(eq(1L))).thenReturn(List.of());

        mockMvc.perform(get("/loan/due-soon")
                .sessionAttr("campus", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void getDueSoonLoansLength_returnsCount() throws Exception {
        when(loanService.getDueSoonLoansLength(eq(1L))).thenReturn(7);

        mockMvc.perform(get("/loan/due-soon/length")
                .sessionAttr("campus", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("7"));
    }

    @Test
    void getTopBooksThisMonth_returnsOk() throws Exception {
        when(loanService.getTopBooksThisMonth(eq(1L))).thenReturn(List.of());

        mockMvc.perform(get("/loan/top-books")
                .sessionAttr("campus", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void getTopGenresThisMonth_returnsOk() throws Exception {
        when(loanService.getTopGenresThisMonth(eq(1L))).thenReturn(List.of());

        mockMvc.perform(get("/loan/top-genres")
                .sessionAttr("campus", "1"))
                .andExpect(status().isOk());
    }
}