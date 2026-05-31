package be.ap.backend.controller;

import java.util.HashSet;
import java.util.Set;

import be.ap.backend.dto.HiddenComponentDTO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.SchoolSettingsDTO;
import be.ap.backend.exception.MissingSessionException;
import be.ap.backend.service.SchoolSettingsService;

import jakarta.servlet.ServletException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SchoolSettingsController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = SchoolSettingsController.class)
public class SchoolSettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SchoolSettingsService schoolSettingsService;

    // Required: SessionContext is constructor-injected; without this bean the
    // application context fails to start and every test would error.
    @MockitoBean
    private SessionContext sessionContext;

    // -------------------------------------------------------------------------
    // GET /school-settings
    // -------------------------------------------------------------------------

    @Test
    void getSettings_returnsOk() throws Exception {
        SchoolSettingsDTO dto = new SchoolSettingsDTO(1L, new HashSet<>());
        when(sessionContext.getSchoolId()).thenReturn(1L);
        when(schoolSettingsService.getSettings(eq(1L))).thenReturn(dto);

        mockMvc.perform(get("/school-settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schoolId").value(1));
    }

    @Test
    void getSettings_emptyHiddenComponents_returnsEmptyArray() throws Exception {
        SchoolSettingsDTO dto = new SchoolSettingsDTO(1L, new HashSet<>());
        when(sessionContext.getSchoolId()).thenReturn(1L);
        when(schoolSettingsService.getSettings(eq(1L))).thenReturn(dto);

        mockMvc.perform(get("/school-settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hiddenComponents").isArray())
                .andExpect(jsonPath("$.hiddenComponents").isEmpty());
    }

    @Test
    void getSettings_noSession_throwsMissingSessionException() {
        when(sessionContext.getSchoolId()).thenReturn(null);

        ServletException ex = assertThrows(ServletException.class,
                () -> mockMvc.perform(get("/school-settings")));

        assertInstanceOf(MissingSessionException.class, ex.getCause());
        verify(schoolSettingsService, never()).getSettings(any());
    }

    // -------------------------------------------------------------------------
    // PUT /school-settings
    // -------------------------------------------------------------------------

    @Test
    void updateSettings_returnsUpdatedDTO() throws Exception {
        SchoolSettingsDTO dto = new SchoolSettingsDTO(1L, new HashSet<>());
        when(sessionContext.getSchoolId()).thenReturn(1L);
        when(schoolSettingsService.updateSettings(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/school-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schoolId").value(1));
    }

    @Test
    void updateSettings_withHiddenComponents_returnsPopulatedDTO() throws Exception {
        Set<HiddenComponentDTO> hidden = Set.of(
                new HiddenComponentDTO("DASHBOARD", "CHART"),
                new HiddenComponentDTO("PROFILE", "AVATAR"));
        SchoolSettingsDTO request = new SchoolSettingsDTO(1L, hidden);
        SchoolSettingsDTO response = new SchoolSettingsDTO(1L, hidden);

        when(sessionContext.getSchoolId()).thenReturn(1L);
        when(schoolSettingsService.updateSettings(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/school-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schoolId").value(1))
                .andExpect(jsonPath("$.hiddenComponents").isArray())
                .andExpect(jsonPath("$.hiddenComponents.length()").value(2));
    }

    @Test
    void updateSettings_noSession_throwsMissingSessionException() {
        SchoolSettingsDTO dto = new SchoolSettingsDTO(1L, Set.of());
        when(sessionContext.getSchoolId()).thenReturn(null);

        ServletException ex = assertThrows(ServletException.class,
                () -> mockMvc.perform(put("/school-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))));

        assertInstanceOf(MissingSessionException.class, ex.getCause());
        verify(schoolSettingsService, never()).updateSettings(any(), any());
    }
}