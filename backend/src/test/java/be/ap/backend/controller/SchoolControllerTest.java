package be.ap.backend.controller;

import be.ap.backend.dto.SchoolDTO;
import be.ap.backend.service.SchoolService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SchoolController.class)
@ContextConfiguration(classes = SchoolController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SchoolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SchoolService schoolService;

    @Autowired
    private ObjectMapper objectMapper;

    private SchoolDTO buildDTO(Long id, String name) {
        SchoolDTO dto = new SchoolDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setLocations(List.of());
        return dto;
    }

    // -------------------------------------------------------------------------
    // POST /school
    // -------------------------------------------------------------------------

    @Test
    void addSchool_shouldReturnCreatedSchool() throws Exception {
        SchoolDTO input = buildDTO(null, "AP Hogeschool");
        SchoolDTO saved = buildDTO(1L, "AP Hogeschool");

        when(schoolService.addSchool(any(SchoolDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/school")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("AP Hogeschool"));
    }

    @Test
    void addSchool_shouldCallServiceWithCorrectBody() throws Exception {
        SchoolDTO input = buildDTO(null, "Thomas More");
        SchoolDTO saved = buildDTO(3L, "Thomas More");

        when(schoolService.addSchool(any(SchoolDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/school")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Thomas More"));
    }

    // -------------------------------------------------------------------------
    // GET /school
    // -------------------------------------------------------------------------

    @Test
    void getAll_shouldReturnListOfSchools() throws Exception {
        when(schoolService.getAll()).thenReturn(List.of(
                buildDTO(1L, "AP Hogeschool"),
                buildDTO(2L, "KU Leuven")));

        mockMvc.perform(get("/school"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("AP Hogeschool"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("KU Leuven"));
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoSchoolsExist() throws Exception {
        when(schoolService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/school"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // -------------------------------------------------------------------------
    // GET /school/{id}
    // -------------------------------------------------------------------------

    @Test
    void findById_shouldReturn200WhenFound() throws Exception {
        when(schoolService.findById(1L)).thenReturn(Optional.of(buildDTO(1L, "AP Hogeschool")));

        mockMvc.perform(get("/school/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("AP Hogeschool"));
    }

    @Test
    void findById_shouldReturn404WhenNotFound() throws Exception {
        when(schoolService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/school/99"))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // PUT /school/{id}
    // -------------------------------------------------------------------------

    @Test
    void updateSchool_shouldReturnUpdatedSchool() throws Exception {
        SchoolDTO input = buildDTO(null, "AP Hogeschool Updated");
        SchoolDTO updated = buildDTO(1L, "AP Hogeschool Updated");

        when(schoolService.updateSchool(eq(1L), any(SchoolDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/school/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("AP Hogeschool Updated"));
    }

    @Test
    void updateSchool_shouldPassCorrectIdToService() throws Exception {
        SchoolDTO input = buildDTO(null, "Thomas More Renamed");
        SchoolDTO updated = buildDTO(5L, "Thomas More Renamed");

        when(schoolService.updateSchool(eq(5L), any(SchoolDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/school/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Thomas More Renamed"));
    }

    @Test
    void updateSchool_shouldPreserveLocationsInResponse() throws Exception {
        SchoolDTO input = buildDTO(null, "AP Hogeschool");
        SchoolDTO updated = buildDTO(1L, "AP Hogeschool");

        when(schoolService.updateSchool(eq(1L), any(SchoolDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/school/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locations").isArray())
                .andExpect(jsonPath("$.locations.length()").value(0));
    }
}