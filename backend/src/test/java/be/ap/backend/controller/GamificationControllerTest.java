package be.ap.backend.controller;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.GamificationDTO;
import be.ap.backend.exception.MissingSessionException;
import be.ap.backend.service.GamificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GamificationControllerTest {

    @Mock
    private GamificationService gamificationService;

    @Mock
    private SessionContext sessionContext;

    @InjectMocks
    private GamificationController gamificationController;

    private final Long USER_ID = 1L;

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void getGamification_correcteData_wordtTeruggegeven() {
        GamificationDTO dto = new GamificationDTO();
        dto.setTotalBooks(5);
        dto.setStreakLevel("5 op rij");

        when(sessionContext.getUserId()).thenReturn(USER_ID);
        when(gamificationService.getGamification(USER_ID)).thenReturn(dto);

        ResponseEntity<GamificationDTO> response = gamificationController.getGamification();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().getTotalBooks());
        assertEquals("5 op rij", response.getBody().getStreakLevel());
    }

    @Test
    void getGamification_delegeertNaarService() {
        when(sessionContext.getUserId()).thenReturn(USER_ID);
        GamificationDTO dto = new GamificationDTO();
        when(gamificationService.getGamification(USER_ID)).thenReturn(dto);

        gamificationController.getGamification();

        verify(gamificationService).getGamification(USER_ID);
    }

    @Test
    void getGamification_geeftServiceResponseTerug() {
        GamificationDTO expected = new GamificationDTO();
        expected.setTotalBooks(99);

        when(sessionContext.getUserId()).thenReturn(USER_ID);
        when(gamificationService.getGamification(USER_ID)).thenReturn(expected);

        ResponseEntity<GamificationDTO> response = gamificationController.getGamification();

        assertSame(expected, response.getBody());
    }

    // -------------------------------------------------------------------------
    // Missing session
    // -------------------------------------------------------------------------

    @Test
    void getGamification_geenUserId_gooidMissingSessionException() {
        when(sessionContext.getUserId()).thenReturn(null);

        assertThrows(MissingSessionException.class,
                () -> gamificationController.getGamification());
    }

    @Test
    void getGamification_geenUserId_serviceWordtNietAangeroepen() {
        when(sessionContext.getUserId()).thenReturn(null);

        try {
            gamificationController.getGamification();
        } catch (MissingSessionException ignored) {
        }

        verifyNoInteractions(gamificationService);
    }

    @Test
    void getGamification_geenUserId_exceptionBevatJuisteBoodschap() {
        when(sessionContext.getUserId()).thenReturn(null);

        MissingSessionException ex = assertThrows(MissingSessionException.class,
                () -> gamificationController.getGamification());

        assertEquals("Niet ingelogd", ex.getMessage());
    }
}
