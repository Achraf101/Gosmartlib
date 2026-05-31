package be.ap.backend.controller;

import be.ap.backend.service.SmartschoolSyncService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SmartschoolSyncControllerTest {

    @Mock
    private SmartschoolSyncService syncService;

    @InjectMocks
    private SmartschoolSyncController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void sync_validSchoolId_returns200WithSubdomain() throws Exception {
        when(syncService.syncSchool(1L)).thenReturn("school-a");

        mockMvc.perform(post("/smartschool/sync/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Sync completed for school: school-a"));

        verify(syncService, times(1)).syncSchool(1L);
    }

    @Test
    void sync_serviceReturnsSubdomain_responseContainsThatSubdomain() throws Exception {
        when(syncService.syncSchool(42L)).thenReturn("my-school");

        mockMvc.perform(post("/smartschool/sync/42"))
                .andExpect(status().isOk())
                .andExpect(content().string("Sync completed for school: my-school"));

        verify(syncService, times(1)).syncSchool(42L);
    }

    @Test
    void sync_serviceThrowsRuntimeException_propagatesAsServletException() {
        when(syncService.syncSchool(1L)).thenThrow(new RuntimeException("Sync failed"));

        ServletException ex = assertThrows(
                ServletException.class,
                () -> mockMvc.perform(post("/smartschool/sync/1")));

        assertInstanceOf(RuntimeException.class, ex.getCause());
        assertEquals("Sync failed", ex.getCause().getMessage());
        verify(syncService, times(1)).syncSchool(1L);
    }

    @Test
    void sync_serviceThrowsIllegalArgumentException_propagatesAsServletException() {
        when(syncService.syncSchool(99L)).thenThrow(new IllegalArgumentException("School not found"));

        ServletException ex = assertThrows(
                ServletException.class,
                () -> mockMvc.perform(post("/smartschool/sync/99")));

        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertEquals("School not found", ex.getCause().getMessage());
        verify(syncService, times(1)).syncSchool(99L);
    }

    @Test
    void sync_callsServiceWithCorrectSchoolId() throws Exception {
        when(syncService.syncSchool(7L)).thenReturn("some-school");

        mockMvc.perform(post("/smartschool/sync/7"))
                .andExpect(status().isOk());

        verify(syncService).syncSchool(7L);
        verifyNoMoreInteractions(syncService);
    }
}