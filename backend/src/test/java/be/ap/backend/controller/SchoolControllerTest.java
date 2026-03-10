package be.ap.backend.controller;

import be.ap.backend.entity.School;
import be.ap.backend.service.SchoolService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SchoolController.class)
@ContextConfiguration(classes = SchoolController.class)
public class SchoolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SchoolService schoolService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addSchool_shouldReturnCreatedSchool() throws Exception {
        School input = new School();
        input.setName("AP Hogeschool");

        School saved = new School();
        saved.setId(1L);
        saved.setName("AP Hogeschool");

        when(schoolService.addSchool(any(School.class))).thenReturn(saved);

        mockMvc.perform(post("/school")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("AP Hogeschool"));
    }

    @Test
    void getAll_shouldReturnListOfSchools() throws Exception {
        School school1 = new School();
        school1.setId(1L);
        school1.setName("AP Hogeschool");

        School school2 = new School();
        school2.setId(2L);
        school2.setName("KU Leuven");

        when(schoolService.getAll()).thenReturn(List.of(school1, school2));

        mockMvc.perform(get("/school"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("AP Hogeschool"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("KU Leuven"));
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoSchoolsExist() throws Exception {
        when(schoolService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/school"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void addSchool_shouldCallServiceWithCorrectBody() throws Exception {
        School input = new School();
        input.setName("Thomas More");

        School saved = new School();
        saved.setId(3L);
        saved.setName("Thomas More");

        when(schoolService.addSchool(any(School.class))).thenReturn(saved);

        mockMvc.perform(post("/school")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Thomas More"));
    }
}