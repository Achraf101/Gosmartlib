package be.ap.backend.controller;

import be.ap.backend.dto.CampusDTO;
import be.ap.backend.service.CampusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CampusController.class)
@ContextConfiguration(classes = CampusController.class)
class CampusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CampusService campusService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createCampus_shouldReturnCreatedCampus() throws Exception {
        CampusDTO dto = new CampusDTO();
        dto.setName("Stad Campus");

        CampusDTO saved = new CampusDTO();
        saved.setId(1L);
        saved.setName("Stad Campus");

        when(campusService.createCampus(any(CampusDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/campus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Stad Campus"));
    }

    @Test
    void createCampus_shouldReturn200_withMinimalDto() throws Exception {
        CampusDTO dto = new CampusDTO();

        CampusDTO saved = new CampusDTO();
        saved.setId(2L);

        when(campusService.createCampus(any(CampusDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/campus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L));
    }

    @Test
    void createCampus_shouldDelegateToService() throws Exception {
        CampusDTO dto = new CampusDTO();
        dto.setName("Noord Campus");

        CampusDTO saved = new CampusDTO();
        saved.setId(3L);
        saved.setName("Noord Campus");

        when(campusService.createCampus(any(CampusDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/campus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Noord Campus"));
    }

    @Test
    void createCampus_shouldReturn400_whenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/campus")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_returnsListOfCampuses() throws Exception {
        CampusDTO dto = new CampusDTO();
        dto.setId(1L);
        dto.setName("Stad Campus");

        when(campusService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/campus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Stad Campus"));
    }

    @Test
    void getAll_empty_returnsEmptyList() throws Exception {
        when(campusService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/campus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getById_exists_returnsCampus() throws Exception {
        CampusDTO dto = new CampusDTO();
        dto.setId(1L);
        dto.setName("Stad Campus");

        when(campusService.findById(1L)).thenReturn(dto);

        mockMvc.perform(get("/campus/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Stad Campus"));
    }

    @Test
    void getById_notFound_throws() throws Exception {
        when(campusService.findById(99L)).thenThrow(new NoSuchElementException());

        assertThatThrownBy(() -> mockMvc.perform(get("/campus/99")))
                .hasCauseInstanceOf(NoSuchElementException.class);
    }
}