package be.ap.backend.controller;

import be.ap.backend.dto.CampusDTO;
import be.ap.backend.entity.Campus;
import be.ap.backend.service.CampusService;
import be.ap.backend.service.SchoolService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CampusController.class)
@ContextConfiguration(classes = CampusController.class)
class CampusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CampusService campusService;

    @MockitoBean
    private SchoolService schoolService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createCampus_shouldReturnCreatedCampus() throws Exception {
        CampusDTO dto = new CampusDTO();
        dto.setName("City Campus");

        Campus saved = new Campus();
        saved.setId(1L);
        saved.setName("City Campus");

        when(campusService.createCampus(any(CampusDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/campus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("City Campus"));
    }

    @Test
    void createCampus_shouldReturn200_withMinimalDto() throws Exception {
        CampusDTO dto = new CampusDTO();

        Campus saved = new Campus();
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
        dto.setName("North Campus");

        Campus saved = new Campus();
        saved.setId(3L);
        saved.setName("North Campus");

        when(campusService.createCampus(any(CampusDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/campus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("North Campus"));
    }

    @Test
    void createCampus_shouldReturn400_whenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/campus")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}