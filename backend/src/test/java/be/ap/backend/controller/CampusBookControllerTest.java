package be.ap.backend.controller;

import be.ap.backend.dto.CampusBookDTO;
import be.ap.backend.dto.CampusBookDetailDTO;
import be.ap.backend.entity.CampusBook;
import be.ap.backend.exception.ArgumentsInvalidException;
import be.ap.backend.exception.BookAlreadyInCampusException;
import be.ap.backend.exception.MissingArgumentsException;
import be.ap.backend.service.CampusBookService;
import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CampusBookController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = CampusBookController.class)
public class CampusBookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CampusBookService campusBookService;

    @Test
    void createCampusBook_success() throws Exception {
        CampusBookDTO dto = new CampusBookDTO();
        dto.setCampusId(1L);
        dto.setBookId(1L);
        dto.setAmount(3);
        dto.setCurrentAmount(3);

        CampusBook saved = new CampusBook();
        when(campusBookService.createCampusBook(any())).thenReturn(saved);

        mockMvc.perform(post("/campusbook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void createCampusBook_missingArguments_returnsBadRequest() throws Exception {
        CampusBookDTO dto = new CampusBookDTO();

        when(campusBookService.createCampusBook(any()))
                .thenThrow(new MissingArgumentsException("Campus en boek zijn verplicht."));

        mockMvc.perform(post("/campusbook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Campus en boek zijn verplicht."));
    }

    @Test
    void createCampusBook_invalidAmount_returnsBadRequest() throws Exception {
        CampusBookDTO dto = new CampusBookDTO();
        dto.setCampusId(1L);
        dto.setBookId(1L);
        dto.setAmount(0);

        when(campusBookService.createCampusBook(any()))
                .thenThrow(new ArgumentsInvalidException("Aantal moet minimaal 1 zijn."));

        mockMvc.perform(post("/campusbook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Aantal moet minimaal 1 zijn."));
    }

    @Test
    void createCampusBook_alreadyExists_returnsConflict() throws Exception {
        CampusBookDTO dto = new CampusBookDTO();
        dto.setCampusId(1L);
        dto.setBookId(1L);
        dto.setAmount(3);

        when(campusBookService.createCampusBook(any()))
                .thenThrow(new BookAlreadyInCampusException("Dit boek is al toegevoegd aan deze campus."));

        mockMvc.perform(post("/campusbook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Dit boek is al toegevoegd aan deze campus."));
    }

    @Test
    void getAll_returnsListOfCampusBooks() throws Exception {
        CampusBookDetailDTO dto = new CampusBookDetailDTO();
        dto.setId(1L);
        dto.setBookTitle("De brief voor de koning");

        when(campusBookService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/campusbook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookTitle").value("De brief voor de koning"));
    }

    @Test
    void getByCampus_returnsPagedResults() throws Exception {
        CampusBookDetailDTO dto = new CampusBookDetailDTO();
        dto.setId(1L);
        dto.setBookTitle("Harry Potter");

        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 5), 1);
        when(campusBookService.findByCampus(eq(1L), eq(0), eq(5))).thenReturn(page);

        mockMvc.perform(get("/campusbook/campus/1?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].bookTitle").value("Harry Potter"));
    }

    @Test
    void getByCampus_empty_returnsEmptyPage() throws Exception {
        var page = new PageImpl<CampusBookDetailDTO>(List.of(), PageRequest.of(0, 5), 0);
        when(campusBookService.findByCampus(eq(1L), eq(0), eq(5))).thenReturn(page);

        mockMvc.perform(get("/campusbook/campus/1?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void getCampusBook_found_returnsOk() throws Exception {
        CampusBookDetailDTO dto = new CampusBookDetailDTO();
        dto.setId(1L);
        dto.setBookTitle("De Hobbit");

        when(campusBookService.getCampusBook(eq(1L), eq(2L))).thenReturn(dto);

        mockMvc.perform(get("/campusbook/1/books/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookTitle").value("De Hobbit"));
    }

    @Test
    void getCampusBook_notFound_returns404() throws Exception {
        when(campusBookService.getCampusBook(eq(1L), eq(99L)))
                .thenThrow(new EntityNotFoundException("CampusBook niet gevonden."));

        mockMvc.perform(get("/campusbook/1/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("CampusBook niet gevonden."));
    }
}