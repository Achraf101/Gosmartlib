package be.ap.backend.controller;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.LocationAvailabilityDTO;
import be.ap.backend.dto.LocationBookDTO;
import be.ap.backend.dto.LocationBookDetailDTO;
import be.ap.backend.dto.SchoolStatsDTO;
import be.ap.backend.exception.ArgumentsInvalidException;
import be.ap.backend.exception.BookAlreadyInLocationException;
import be.ap.backend.exception.GlobalExceptionHandler;
import be.ap.backend.exception.MissingArgumentsException;
import be.ap.backend.service.LocationBookService;
import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
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

@WebMvcTest(controllers = LocationBookController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = { LocationBookController.class, GlobalExceptionHandler.class })
public class LocationBookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LocationBookService locationBookService;

    @MockitoBean
    private SessionContext sessionContext;

    // -------------------------------------------------------------------------
    // POST /locationbook
    // -------------------------------------------------------------------------

    @Test
    void createLocationBook_success() throws Exception {
        LocationBookDTO dto = new LocationBookDTO();
        dto.setLocationId(1L);
        dto.setBookId(1L);
        dto.setAmount(3);
        dto.setCurrentAmount(3);

        LocationBookDetailDTO saved = new LocationBookDetailDTO();
        when(locationBookService.createLocationBook(any())).thenReturn(saved);

        mockMvc.perform(post("/locationbook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void createLocationBook_missingArguments_returnsBadRequest() throws Exception {
        LocationBookDTO dto = new LocationBookDTO();

        when(locationBookService.createLocationBook(any()))
                .thenThrow(new MissingArgumentsException("Locatie en boek zijn verplicht."));

        mockMvc.perform(post("/locationbook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Locatie en boek zijn verplicht."));
    }

    @Test
    void createLocationBook_invalidAmount_returnsBadRequest() throws Exception {
        LocationBookDTO dto = new LocationBookDTO();
        dto.setLocationId(1L);
        dto.setBookId(1L);
        dto.setAmount(0);

        when(locationBookService.createLocationBook(any()))
                .thenThrow(new ArgumentsInvalidException("Aantal moet minimaal 1 zijn."));

        mockMvc.perform(post("/locationbook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Aantal moet minimaal 1 zijn."));
    }

    @Test
    void createLocationBook_alreadyExists_returnsConflict() throws Exception {
        LocationBookDTO dto = new LocationBookDTO();
        dto.setLocationId(1L);
        dto.setBookId(1L);
        dto.setAmount(3);

        when(locationBookService.createLocationBook(any()))
                .thenThrow(new BookAlreadyInLocationException("Dit boek is al toegevoegd aan deze locatie."));

        mockMvc.perform(post("/locationbook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Dit boek is al toegevoegd aan deze locatie."));
    }

    // -------------------------------------------------------------------------
    // GET /locationbook
    // -------------------------------------------------------------------------

    @Test
    void getAll_returnsListOfLocationBooks() throws Exception {
        LocationBookDetailDTO dto = new LocationBookDetailDTO();
        dto.setId(1L);
        dto.setBookTitle("De brief voor de koning");

        when(locationBookService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/locationbook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookTitle").value("De brief voor de koning"));
    }

    // -------------------------------------------------------------------------
    // GET /locationbook/location/{locationId}
    // -------------------------------------------------------------------------

    @Test
    void getByLocation_returnsPagedResults() throws Exception {
        LocationBookDetailDTO dto = new LocationBookDetailDTO();
        dto.setId(1L);
        dto.setBookTitle("Harry Potter");

        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 5), 1);
        when(locationBookService.findByLocation(eq(1L), eq(0), eq(5))).thenReturn(page);

        mockMvc.perform(get("/locationbook/location/1?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].bookTitle").value("Harry Potter"));
    }

    @Test
    void getByLocation_empty_returnsEmptyPage() throws Exception {
        var page = new PageImpl<LocationBookDetailDTO>(List.of(), PageRequest.of(0, 5), 0);
        when(locationBookService.findByLocation(eq(1L), eq(0), eq(5))).thenReturn(page);

        mockMvc.perform(get("/locationbook/location/1?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    // -------------------------------------------------------------------------
    // GET /locationbook/{locationId}/books/{bookId}
    // -------------------------------------------------------------------------

    @Test
    void getLocationBook_found_returnsOk() throws Exception {
        LocationBookDetailDTO dto = new LocationBookDetailDTO();
        dto.setId(1L);
        dto.setBookTitle("De Hobbit");

        when(locationBookService.getLocationBook(eq(1L), eq(2L))).thenReturn(dto);

        mockMvc.perform(get("/locationbook/1/books/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookTitle").value("De Hobbit"));
    }

    @Test
    void getLocationBook_notFound_returns404() throws Exception {
        when(locationBookService.getLocationBook(eq(1L), eq(99L)))
                .thenThrow(new EntityNotFoundException("LocationBook niet gevonden."));

        mockMvc.perform(get("/locationbook/1/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("LocationBook niet gevonden."));
    }

    // -------------------------------------------------------------------------
    // GET /locationbook/book/{bookId}
    // -------------------------------------------------------------------------
    @Test
    void getAvailabilityByBook_withValidSession_returnsOk() throws Exception {
        LocationAvailabilityDTO availability = new LocationAvailabilityDTO();
        availability.setLocationId(1L);
        availability.setAmount(5);
        availability.setCurrentAmount(4);

        when(sessionContext.getSchoolId()).thenReturn(10L);
        when(locationBookService.getAvailabilityByBook(eq(5L), eq(10L)))
                .thenReturn(List.of(availability));

        mockMvc.perform(get("/locationbook/book/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].locationId").value(1))
                .andExpect(jsonPath("$[0].amount").value(5))
                .andExpect(jsonPath("$[0].currentAmount").value(4));
    }

    @Test
    void getAvailabilityByBook_noSession_returnsUnauthorized() throws Exception {
        when(sessionContext.getSchoolId()).thenReturn(null);

        mockMvc.perform(get("/locationbook/book/5"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAvailabilityByBook_emptyList_returnsOk() throws Exception {
        when(sessionContext.getSchoolId()).thenReturn(10L);
        when(locationBookService.getAvailabilityByBook(eq(5L), eq(10L)))
                .thenReturn(List.of());

        mockMvc.perform(get("/locationbook/book/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // -------------------------------------------------------------------------
    // GET /locationbook/stats
    // -------------------------------------------------------------------------

    @Test
    void getSchoolStats_withValidSession_returnsOk() throws Exception {
        SchoolStatsDTO stats = new SchoolStatsDTO(42, 10);

        when(sessionContext.getSchoolId()).thenReturn(10L);
        when(locationBookService.getStatsForSchool(eq(10L))).thenReturn(stats);

        mockMvc.perform(get("/locationbook/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBooks").value(42))
                .andExpect(jsonPath("$.availableBooks").value(10));
    }

    @Test
    void getSchoolStats_noSession_returnsUnauthorized() throws Exception {
        when(sessionContext.getSchoolId()).thenReturn(null);

        mockMvc.perform(get("/locationbook/stats"))
                .andExpect(status().isUnauthorized());
    }
}