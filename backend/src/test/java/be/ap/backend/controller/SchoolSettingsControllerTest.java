package be.ap.backend.controller;

import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import be.ap.backend.dto.SchoolSettingsDTO;
import be.ap.backend.service.SchoolSettingsService;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;

@WebMvcTest(controllers = SchoolSettingsController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = SchoolSettingsController.class)
public class SchoolSettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SchoolSettingsService schoolSettingsService;

    @Test
    void getSettings_returnsOk() throws Exception {
        SchoolSettingsDTO dto = new SchoolSettingsDTO(1L, new HashSet<>());
        when(schoolSettingsService.getSettings(eq(1L))).thenReturn(dto);

        mockMvc.perform(get("/school-settings")
                .sessionAttr("school", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schoolId").value(1));
    }

    @Test
    void updateSettings_returnsUpdatedDTO() throws Exception {
        SchoolSettingsDTO dto = new SchoolSettingsDTO(1L, new HashSet<>());
        when(schoolSettingsService.updateSettings(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/school-settings")
                .sessionAttr("school", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schoolId").value(1));
    }

    @Test
    void getSettings_emptyHiddenComponents_returnsEmptyList() throws Exception {
        SchoolSettingsDTO dto = new SchoolSettingsDTO(1L, new HashSet<>());
        when(schoolSettingsService.getSettings(eq(1L))).thenReturn(dto);

        mockMvc.perform(get("/school-settings")
                .sessionAttr("school", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hiddenComponents").isArray());
    }
}