package be.ap.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import be.ap.backend.config.TestSecurityConfig;
import be.ap.backend.entity.User;
import be.ap.backend.repository.UserRepository;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setPassword("hashed_password");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePassword_success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("currentPass", "hashed_password")).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("new_hashed_password");

        mockMvc.perform(put("/account/password")
                .sessionAttr("userId", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "currentPassword": "currentPass",
                            "newPassword": "newPass123"
                        }
                        """))
                .andExpect(status().isOk());

        verify(userRepository).save(mockUser);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePassword_userNotFound_returns404() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/account/password")
                .sessionAttr("userId", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "currentPassword": "currentPass",
                            "newPassword": "newPass123"
                        }
                        """))
                .andExpect(status().isNotFound());

        verify(userRepository, never()).save(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePassword_wrongCurrentPassword_returns400() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongPass", "hashed_password")).thenReturn(false);

        mockMvc.perform(put("/account/password")
                .sessionAttr("userId", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "currentPassword": "wrongPass",
                            "newPassword": "newPass123"
                        }
                        """))
                .andExpect(status().isBadRequest());

        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePassword_notAuthenticated_returns400() throws Exception {
        mockMvc.perform(put("/account/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "currentPassword": "currentPass",
                            "newPassword": "newPass123"
                        }
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updatePassword_wrongRole_returns403() throws Exception {
        mockMvc.perform(put("/account/password")
                .sessionAttr("userId", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "currentPassword": "currentPass",
                            "newPassword": "newPass123"
                        }
                        """))
                .andExpect(status().isForbidden());
    }
}