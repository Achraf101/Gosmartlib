package be.ap.backend.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import be.ap.backend.config.SessionContext;
import be.ap.backend.config.TestSecurityConfig;
import be.ap.backend.dto.PasswordDTO;
import be.ap.backend.exception.ArgumentsInvalidException;
import be.ap.backend.exception.GlobalExceptionHandler;
import be.ap.backend.service.AccountService;
import jakarta.persistence.EntityNotFoundException;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc
@Import({ TestSecurityConfig.class, GlobalExceptionHandler.class })
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private SessionContext session;

    private static final String ENDPOINT = "/account/password";
    private static final String VALID_BODY = """
            {
                "currentPassword": "currentPass",
                "newPassword": "newPass123"
            }
            """;

    @BeforeEach
    void setUp() {
        lenient().when(session.getUserId()).thenReturn(1L); // lenient avoids UnnecessaryStubbingException
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePassword_success() throws Exception {
        doNothing().when(accountService).updatePassword(any(Long.class), any(PasswordDTO.class));

        mockMvc.perform(put(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
                .andExpect(status().isOk());

        verify(accountService).updatePassword(1L, new PasswordDTO("currentPass", "newPass123"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePassword_userNotFound_returns404() throws Exception {
        doThrow(new EntityNotFoundException("Gebruiker niet gevonden."))
                .when(accountService).updatePassword(any(Long.class), any(PasswordDTO.class));

        mockMvc.perform(put(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
                .andExpect(status().isNotFound());

        verify(accountService).updatePassword(eq(1L), any(PasswordDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePassword_wrongCurrentPassword_returns400() throws Exception {
        doThrow(new ArgumentsInvalidException("Huidig wachtwoord is onjuist."))
                .when(accountService).updatePassword(any(Long.class), any(PasswordDTO.class));

        mockMvc.perform(put(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
                .andExpect(status().isBadRequest());

        verify(accountService).updatePassword(eq(1L), any(PasswordDTO.class));
    }

    @Test
    void updatePassword_notAuthenticated_returns401() throws Exception {
        mockMvc.perform(put(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
                .andExpect(status().isUnauthorized());

        verify(accountService, never()).updatePassword(any(), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updatePassword_wrongRole_returns403() throws Exception {
        mockMvc.perform(put(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
                .andExpect(status().isForbidden());

        verify(accountService, never()).updatePassword(any(), any());
    }
}