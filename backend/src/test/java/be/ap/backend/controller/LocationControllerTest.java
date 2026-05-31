package be.ap.backend.controller;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.LocationDTO;
import be.ap.backend.entity.UserRole;
import be.ap.backend.service.LocationService;
import jakarta.servlet.ServletException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LocationController.class)
@ContextConfiguration(classes = LocationController.class)
@AutoConfigureMockMvc(addFilters = false)
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LocationService locationService;

    @MockitoBean
    private SessionContext sessionContext;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // POST /location
    // -------------------------------------------------------------------------

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .alwaysExpect(status -> {
                }) // no-op — just overrides default rethrowing
                .build();
    }

    @Test
    void createLocation_shouldReturnCreatedLocation() throws Exception {
        LocationDTO dto = new LocationDTO();
        dto.setName("Stad Campus");

        LocationDTO saved = new LocationDTO();
        saved.setId(1L);
        saved.setName("Stad Campus");

        when(locationService.createLocation(any(LocationDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/location")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Stad Campus"));
    }

    @Test
    void createLocation_shouldReturn200_withMinimalDto() throws Exception {
        LocationDTO dto = new LocationDTO();

        LocationDTO saved = new LocationDTO();
        saved.setId(2L);

        when(locationService.createLocation(any(LocationDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/location")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L));
    }

    @Test
    void createLocation_shouldDelegateToService() throws Exception {
        LocationDTO dto = new LocationDTO();
        dto.setName("Noord Campus");

        LocationDTO saved = new LocationDTO();
        saved.setId(3L);
        saved.setName("Noord Campus");

        when(locationService.createLocation(any(LocationDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/location")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Noord Campus"));
    }

    @Test
    void createLocation_shouldReturn400_whenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/location")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /location — ADMIN
    // -------------------------------------------------------------------------

    @Test
    void getAll_asAdmin_returnsAllLocations() throws Exception {
        LocationDTO dto = new LocationDTO();
        dto.setId(1L);
        dto.setName("Stad Campus");

        when(sessionContext.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(locationService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/location"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Stad Campus"));
    }

    @Test
    void getAll_asAdmin_empty_returnsEmptyList() throws Exception {
        when(sessionContext.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(locationService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/location"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // -------------------------------------------------------------------------
    // GET /location — BIBLIOTHEEKBEHEERDER
    // -------------------------------------------------------------------------

    @Test
    void getAll_asBibliotheekbeheerder_returnsLocationsForSchool() throws Exception {
        LocationDTO dto = new LocationDTO();
        dto.setId(5L);
        dto.setName("Noord Campus");

        when(sessionContext.hasRole(UserRole.ADMIN)).thenReturn(false);
        when(sessionContext.hasRole(UserRole.BIBLIOTHEEKBEHEERDER)).thenReturn(true);
        when(sessionContext.getSchoolId()).thenReturn(42L);
        when(locationService.getLocationsBySchool(42L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/location"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5L))
                .andExpect(jsonPath("$[0].name").value("Noord Campus"));
    }

    @Test
    void getAll_asBibliotheekbeheerder_empty_returnsEmptyList() throws Exception {
        when(sessionContext.hasRole(UserRole.ADMIN)).thenReturn(false);
        when(sessionContext.hasRole(UserRole.BIBLIOTHEEKBEHEERDER)).thenReturn(true);
        when(sessionContext.getSchoolId()).thenReturn(42L);
        when(locationService.getLocationsBySchool(42L)).thenReturn(List.of());

        mockMvc.perform(get("/location"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // -------------------------------------------------------------------------
    // GET /location — insufficient role
    // -------------------------------------------------------------------------

    @Test
    void getAll_withoutRequiredRole_returnsForbidden() throws Exception {
        when(sessionContext.hasRole(UserRole.ADMIN)).thenReturn(false);
        when(sessionContext.hasRole(UserRole.BIBLIOTHEEKBEHEERDER)).thenReturn(false);

        mockMvc.perform(get("/location"))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // GET /location/{id}
    // -------------------------------------------------------------------------

    @Test
    void getById_exists_returnsLocation() throws Exception {
        LocationDTO dto = new LocationDTO();
        dto.setId(1L);
        dto.setName("Stad Campus");

        when(locationService.findById(1L)).thenReturn(dto);

        mockMvc.perform(get("/location/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Stad Campus"));
    }

    @Test
    void getById_notFound_throwsNoSuchElement() {
        when(locationService.findById(99L)).thenThrow(new NoSuchElementException("not found"));

        assertThatThrownBy(() -> mockMvc.perform(get("/location/99")))
                .isInstanceOf(ServletException.class)
                .hasCauseInstanceOf(NoSuchElementException.class);
    }
}