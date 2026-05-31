package be.ap.backend.controller;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.LoanDTO;
import be.ap.backend.dto.TopBookDTO;
import be.ap.backend.dto.UpdateNoteDTO;
import be.ap.backend.dto.UpdateStatusDTO;
import be.ap.backend.enums.LoanStatus;
import be.ap.backend.exception.MissingSessionException;
import be.ap.backend.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LoanController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = { LoanController.class, SessionContext.class })
public class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoanService loanService;

    @MockitoBean
    private SessionContext sessionContext;

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
    void createLoan_withLongSessionUserId_setsUserIdOnDto() throws Exception {
        when(sessionContext.getUserId()).thenReturn(1L);
        LoanDTO dto = new LoanDTO();
        when(loanService.createLoan(any())).thenReturn(List.of(dto));

        mockMvc.perform(post("/loan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoanDTO())))
                .andExpect(status().isOk());

        verify(loanService).createLoan(argThat(d -> Long.valueOf(1L).equals(d.getUserId())));
    }

    @Test
    void createLoan_withStringSessionUserId_setsUserIdOnDto() throws Exception {
        when(sessionContext.getUserId()).thenReturn(2L);
        LoanDTO dto = new LoanDTO();
        when(loanService.createLoan(any())).thenReturn(List.of(dto));

        mockMvc.perform(post("/loan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoanDTO())))
                .andExpect(status().isOk());

        verify(loanService).createLoan(argThat(d -> Long.valueOf(2L).equals(d.getUserId())));
    }

    @Test
    void createLoan_noSessionUserId_throwsMissingSessionException() throws Exception {
        when(sessionContext.getUserId()).thenReturn(null);

        assertThatThrownBy(() -> mockMvc.perform(post("/loan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoanDTO()))))
                .cause()
                .isInstanceOf(MissingSessionException.class)
                .hasMessageContaining("Niet ingelogd");
    }

    @Test
    void createLoan_serviceThrowsIllegalArgument_propagatesException() throws Exception {
        when(sessionContext.getUserId()).thenReturn(1L);
        when(loanService.createLoan(any()))
                .thenThrow(new IllegalArgumentException("minstens 1 boek is verplicht"));

        assertThatThrownBy(() -> mockMvc.perform(post("/loan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoanDTO()))))
                .cause()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minstens 1 boek is verplicht");
    }

    @Test
    void createLoan_serviceThrowsEntityNotFound_propagatesException() throws Exception {
        when(sessionContext.getUserId()).thenReturn(1L);
        when(loanService.createLoan(any()))
                .thenThrow(new EntityNotFoundException("Gebruiker niet gevonden"));

        assertThatThrownBy(() -> mockMvc.perform(post("/loan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoanDTO()))))
                .cause()
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Gebruiker niet gevonden");
    }

    @Test
    void createLoan_multipleBooks_returnsMultipleLoans() throws Exception {
        when(sessionContext.getUserId()).thenReturn(1L);
        when(loanService.createLoan(any())).thenReturn(List.of(new LoanDTO(), new LoanDTO()));

        mockMvc.perform(post("/loan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoanDTO())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ── PUT /loan/{id}/note ───────────────────────────────────────

    @Test
    void updateNote_success() throws Exception {
        UpdateNoteDTO updateNoteDTO = new UpdateNoteDTO("Opmerking");
        when(loanService.updateNote(eq(1L), eq("Opmerking"))).thenReturn(new LoanDTO());

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
        UpdateStatusDTO dto = new UpdateStatusDTO(LoanStatus.ACCEPTED);
        when(loanService.updateStatus(eq(1L), eq(LoanStatus.ACCEPTED))).thenReturn(new LoanDTO());

        mockMvc.perform(put("/loan/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatus_serviceThrowsEntityNotFound_propagatesException() throws Exception {
        UpdateStatusDTO dto = new UpdateStatusDTO(LoanStatus.ACCEPTED);
        when(loanService.updateStatus(eq(99L), any()))
                .thenThrow(new EntityNotFoundException("Loan niet gevonden met id: 99"));

        assertThatThrownBy(() -> mockMvc.perform(put("/loan/99/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))))
                .cause()
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Loan niet gevonden met id: 99");
    }

    // ── PUT /loan/{id}/scan-pickup ────────────────────────────────

    @Test
    void scanPickup_success_returnsOk() throws Exception {
        when(loanService.scanPickup(eq(1L), eq(10L))).thenReturn(new LoanDTO());

        mockMvc.perform(put("/loan/1/scan-pickup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("book_copy_id", 10))))
                .andExpect(status().isOk());

        verify(loanService).scanPickup(1L, 10L);
    }

    @Test
    void scanPickup_serviceThrowsEntityNotFound_propagatesException() throws Exception {
        when(loanService.scanPickup(eq(99L), any()))
                .thenThrow(new EntityNotFoundException("Loan niet gevonden met id: 99"));

        assertThatThrownBy(() -> mockMvc.perform(put("/loan/99/scan-pickup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("book_copy_id", 10)))))
                .cause()
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Loan niet gevonden met id: 99");
    }

    @Test
    void scanPickup_serviceThrowsIllegalArgument_propagatesException() throws Exception {
        when(loanService.scanPickup(eq(1L), eq(99L)))
                .thenThrow(new IllegalArgumentException("Boekexemplaar komt niet overeen"));

        assertThatThrownBy(() -> mockMvc.perform(put("/loan/1/scan-pickup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("book_copy_id", 99)))))
                .cause()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Boekexemplaar komt niet overeen");
    }

    // ── PUT /loan/{id}/scan-return ────────────────────────────────

    @Test
    void scanReturn_success_notDamaged_returnsOk() throws Exception {
        when(loanService.scanReturn(eq(1L), eq(10L), eq(null), eq(false)))
                .thenReturn(new LoanDTO());

        mockMvc.perform(put("/loan/1/scan-return")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("book_copy_id", 10, "damaged", false))))
                .andExpect(status().isOk());

        verify(loanService).scanReturn(1L, 10L, null, false);
    }

    @Test
    void scanReturn_withNoteAndDamaged_passesCorrectArgs() throws Exception {
        when(loanService.scanReturn(eq(1L), eq(10L), eq("Omslag beschadigd"), eq(true)))
                .thenReturn(new LoanDTO());

        mockMvc.perform(put("/loan/1/scan-return")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("book_copy_id", 10, "note", "Omslag beschadigd", "damaged", true))))
                .andExpect(status().isOk());

        verify(loanService).scanReturn(1L, 10L, "Omslag beschadigd", true);
    }

    @Test
    void scanReturn_serviceThrowsEntityNotFound_propagatesException() throws Exception {
        when(loanService.scanReturn(eq(99L), any(), any(), anyBoolean()))
                .thenThrow(new EntityNotFoundException("Loan niet gevonden met id: 99"));

        assertThatThrownBy(() -> mockMvc.perform(put("/loan/99/scan-return")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("book_copy_id", 10, "damaged", false)))))
                .cause()
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Loan niet gevonden met id: 99");
    }

    // ── PUT /loan/{id}/pickup ─────────────────────────────────────

    @Test
    void pickupLoan_withBookCopyId_passesIdToService() throws Exception {
        when(loanService.pickupLoan(eq(1L), eq(5L))).thenReturn(new LoanDTO());

        mockMvc.perform(put("/loan/1/pickup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("book_copy_id", 5))))
                .andExpect(status().isOk());

        verify(loanService).pickupLoan(1L, 5L);
    }

    @Test
    void pickupLoan_withoutBody_passesNullBookCopyIdToService() throws Exception {
        when(loanService.pickupLoan(eq(1L), eq(null))).thenReturn(new LoanDTO());

        mockMvc.perform(put("/loan/1/pickup"))
                .andExpect(status().isOk());

        verify(loanService).pickupLoan(1L, null);
    }

    @Test
    void pickupLoan_withEmptyBody_passesNullBookCopyIdToService() throws Exception {
        when(loanService.pickupLoan(eq(1L), eq(null))).thenReturn(new LoanDTO());

        mockMvc.perform(put("/loan/1/pickup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());

        verify(loanService).pickupLoan(1L, null);
    }

    @Test
    void pickupLoan_serviceThrowsEntityNotFound_propagatesException() throws Exception {
        when(loanService.pickupLoan(eq(99L), any()))
                .thenThrow(new EntityNotFoundException("Loan niet gevonden met id: 99"));

        assertThatThrownBy(() -> mockMvc.perform(put("/loan/99/pickup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")))
                .cause()
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Loan niet gevonden met id: 99");
    }

    // ── GET /loan/user ────────────────────────────────────────────

    @Test
    void getByUserId_withLongSessionUserId_returnsLoans() throws Exception {
        when(sessionContext.getUserId()).thenReturn(1L);
        when(loanService.getByUserId(1L)).thenReturn(List.of(new LoanDTO()));

        mockMvc.perform(get("/loan/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByUserId_withStringSessionUserId_returnsLoans() throws Exception {
        when(sessionContext.getUserId()).thenReturn(2L);
        when(loanService.getByUserId(2L)).thenReturn(List.of(new LoanDTO()));

        mockMvc.perform(get("/loan/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByUserId_noSession_throwsMissingSessionException() throws Exception {
        when(sessionContext.getUserId()).thenReturn(null);

        assertThatThrownBy(() -> mockMvc.perform(get("/loan/user")))
                .cause()
                .isInstanceOf(MissingSessionException.class)
                .hasMessageContaining("Niet ingelogd");
    }

    // ── PUT /loan/{id}/extend ─────────────────────────────────────

    @Test
    void extendLoan_success_returnsOk() throws Exception {
        when(loanService.extendLoan(1L)).thenReturn(new LoanDTO());

        mockMvc.perform(put("/loan/1/extend"))
                .andExpect(status().isOk());
    }

    @Test
    void extendLoan_notFound_propagatesEntityNotFoundException() throws Exception {
        when(loanService.extendLoan(99L))
                .thenThrow(new EntityNotFoundException("Lening niet gevonden met id: 99"));

        assertThatThrownBy(() -> mockMvc.perform(put("/loan/99/extend")))
                .cause()
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Lening niet gevonden met id: 99");
    }

    @Test
    void extendLoan_maxExtensionsReached_propagatesIllegalArgumentException() throws Exception {
        when(loanService.extendLoan(1L))
                .thenThrow(new IllegalArgumentException("Maximum aantal verlengingen bereikt."));

        assertThatThrownBy(() -> mockMvc.perform(put("/loan/1/extend")))
                .cause()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Maximum aantal verlengingen bereikt.");
    }

    @Test
    void extendLoan_wrongStatus_propagatesIllegalArgumentException() throws Exception {
        when(loanService.extendLoan(1L))
                .thenThrow(new IllegalArgumentException("Lening kan niet verlengd worden met status: REQUESTED"));

        assertThatThrownBy(() -> mockMvc.perform(put("/loan/1/extend")))
                .cause()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lening kan niet verlengd worden met status: REQUESTED");
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

    // ── GET /loan/overdue ─────────────────────────────────────────

    @Test
    void getOverdueLoans_returnsOk() throws Exception {
        when(sessionContext.getSchoolId()).thenReturn(1L);
        when(loanService.getOverdueLoans(1L)).thenReturn(List.of());

        mockMvc.perform(get("/loan/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getOverdueLoans_noSession_throwsMissingSessionException() throws Exception {
        when(sessionContext.getSchoolId()).thenReturn(null);

        assertThatThrownBy(() -> mockMvc.perform(get("/loan/overdue")))
                .cause()
                .isInstanceOf(MissingSessionException.class)
                .hasMessageContaining("Niet ingelogd");
    }

    @Test
    void getOverdueLoansLength_returnsCount() throws Exception {
        when(sessionContext.getSchoolId()).thenReturn(1L);
        when(loanService.getOverdueLoansLength(1L)).thenReturn(3);

        mockMvc.perform(get("/loan/overdue/length"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    void getOverdueLoansLength_noSession_throwsMissingSessionException() throws Exception {
        when(sessionContext.getSchoolId()).thenReturn(null);

        assertThatThrownBy(() -> mockMvc.perform(get("/loan/overdue/length")))
                .cause()
                .isInstanceOf(MissingSessionException.class)
                .hasMessageContaining("Niet ingelogd");
    }

    // ── GET /loan/due-soon ────────────────────────────────────────

    @Test
    void getDueSoonLoans_returnsOk() throws Exception {
        when(sessionContext.getSchoolId()).thenReturn(1L);
        when(loanService.getDueSoonLoans(1L)).thenReturn(List.of());

        mockMvc.perform(get("/loan/due-soon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getDueSoonLoans_noSession_throwsMissingSessionException() throws Exception {
        when(sessionContext.getSchoolId()).thenReturn(null);

        assertThatThrownBy(() -> mockMvc.perform(get("/loan/due-soon")))
                .cause()
                .isInstanceOf(MissingSessionException.class)
                .hasMessageContaining("Niet ingelogd");
    }

    @Test
    void getDueSoonLoansLength_returnsCount() throws Exception {
        when(sessionContext.getSchoolId()).thenReturn(1L);
        when(loanService.getDueSoonLoansLength(1L)).thenReturn(7);

        mockMvc.perform(get("/loan/due-soon/length"))
                .andExpect(status().isOk())
                .andExpect(content().string("7"));
    }

    @Test
    void getDueSoonLoansLength_noSession_throwsMissingSessionException() throws Exception {
        when(sessionContext.getSchoolId()).thenReturn(null);

        assertThatThrownBy(() -> mockMvc.perform(get("/loan/due-soon/length")))
                .cause()
                .isInstanceOf(MissingSessionException.class)
                .hasMessageContaining("Niet ingelogd");
    }

    // ── GET /loan/top-books ───────────────────────────────────────

    @Test
    void getTopBooksThisMonth_returnsOk() throws Exception {
        when(sessionContext.getSchoolId()).thenReturn(1L);
        when(loanService.getTopBooksThisMonth(1L)).thenReturn(List.of(new TopBookDTO("Test Book", 5)));

        mockMvc.perform(get("/loan/top-books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getTopBooksThisMonth_noSession_throwsMissingSessionException() throws Exception {
        when(sessionContext.getSchoolId()).thenReturn(null);

        assertThatThrownBy(() -> mockMvc.perform(get("/loan/top-books")))
                .cause()
                .isInstanceOf(MissingSessionException.class)
                .hasMessageContaining("Niet ingelogd");
    }

    // ── GET /loan/top-genres ──────────────────────────────────────

    @Test
    void getTopGenresThisMonth_returnsOk() throws Exception {
        when(sessionContext.getSchoolId()).thenReturn(1L);
        when(loanService.getTopGenresThisMonth(1L)).thenReturn(List.of(new TopBookDTO("Fantasy", 3)));

        mockMvc.perform(get("/loan/top-genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getTopGenresThisMonth_noSession_throwsMissingSessionException() throws Exception {
        when(sessionContext.getSchoolId()).thenReturn(null);

        assertThatThrownBy(() -> mockMvc.perform(get("/loan/top-genres")))
                .cause()
                .isInstanceOf(MissingSessionException.class)
                .hasMessageContaining("Niet ingelogd");
    }

    // ── GET /loan/state/{state} ───────────────────────────────────

    @Test
    void getByStateAndSchool_returnsLoans() throws Exception {
        when(sessionContext.getSchoolId()).thenReturn(1L);
        when(loanService.getByStateAndSchool(eq(LoanStatus.ACCEPTED), eq(1L)))
                .thenReturn(List.of(new LoanDTO()));

        mockMvc.perform(get("/loan/state/ACCEPTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByStateAndSchool_empty_returnsEmptyList() throws Exception {
        when(sessionContext.getSchoolId()).thenReturn(1L);
        when(loanService.getByStateAndSchool(eq(LoanStatus.ACCEPTED), eq(1L)))
                .thenReturn(List.of());

        mockMvc.perform(get("/loan/state/ACCEPTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getByStateAndSchool_noSession_throwsMissingSessionException() throws Exception {
        when(sessionContext.getSchoolId()).thenReturn(null);

        assertThatThrownBy(() -> mockMvc.perform(get("/loan/state/ACCEPTED")))
                .cause()
                .isInstanceOf(MissingSessionException.class)
                .hasMessageContaining("Niet ingelogd");
    }
}