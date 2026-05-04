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

import be.ap.backend.dto.CampusSettingsDTO;
import be.ap.backend.service.CampusSettingsService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;

@WebMvcTest(controllers = CampusSettingsController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = CampusSettingsController.class)
public class CampusSettingsControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private CampusSettingsService campusSettingsService;

    @Test
    void getSettings_returnsOk() throws Exception {
        CampusSettingsDTO dto = new CampusSettingsDTO(1L, new HashSet<>());
        when(campusSettingsService.getSettings(eq(1L))).thenReturn(dto);

        mockMvc.perform(get("/campus-settings")
                .sessionAttr("campus", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campusId").value(1));
    }

    @Test
    void updateSettings_returnsUpdatedDTO() throws Exception {
        CampusSettingsDTO dto = new CampusSettingsDTO(1L, new HashSet<>());
        when(campusSettingsService.updateSettings(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/campus-settings")
                .sessionAttr("campus", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campusId").value(1));
    }

    @Test
    void getSettings_emptyHiddenComponents_returnsEmptyList() throws Exception {
        CampusSettingsDTO dto = new CampusSettingsDTO(1L, new HashSet<>());
        when(campusSettingsService.getSettings(eq(1L))).thenReturn(dto);

        mockMvc.perform(get("/campus-settings")
                .sessionAttr("campus", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hiddenComponents").isArray());
    }
}
