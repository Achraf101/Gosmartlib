package be.ap.backend.controller;

import be.ap.backend.dto.LoanDTO;
import be.ap.backend.dto.UpdateNoteDTO;
import be.ap.backend.dto.UpdateStatusDTO;
import be.ap.backend.entity.LoanStatus;
import be.ap.backend.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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

    @Test
    void getRequested_returnsListOfLoans() throws Exception {
        LoanDTO dto = new LoanDTO();
        // stel een veld in dat jouw LoanDTO heeft, bv:
        // dto.setId(1L);

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

    @Test
    void createLoan_success() throws Exception {
        LoanDTO dto = new LoanDTO();
        // dto.setStudentId(1L);
        // dto.setCampusBookId(1L);

        when(loanService.createLoan(any())).thenReturn(dto);

        mockMvc.perform(post("/loan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateNote_success() throws Exception {
        LoanDTO result = new LoanDTO();
        // result.setNote("Opmerking");

        UpdateNoteDTO updateNoteDTO = new UpdateNoteDTO("Opmerking");

        when(loanService.updateNote(eq(1L), eq("Opmerking"))).thenReturn(result);

        mockMvc.perform(put("/loan/1/note")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateNoteDTO)))
                .andExpect(status().isOk());
    }

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
}