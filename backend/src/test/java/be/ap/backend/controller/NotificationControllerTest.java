package be.ap.backend.controller;

import be.ap.backend.exception.GlobalExceptionHandler;
import be.ap.backend.queue.NotificationTask;
import be.ap.backend.queue.TaskQueueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import({ NotificationControllerTest.MethodSecurityConfig.class, GlobalExceptionHandler.class })
class NotificationControllerTest {

    @EnableMethodSecurity
    static class MethodSecurityConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskQueueService taskQueueService;

    // --- Authorization ---

    @Test
    void shouldReturnUnauthorizedWhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/notify/1").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "LEZER")
    void shouldReturnForbiddenWhenWrongRole() throws Exception {
        mockMvc.perform(post("/notify/1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    // --- Happy path ---

    @Test
    @WithMockUser(roles = "BIBLIOTHEEKBEHEERDER")
    void shouldReturnTrueWhenLoanIdIsValid() throws Exception {
        mockMvc.perform(post("/notify/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockUser(roles = "BIBLIOTHEEKBEHEERDER")
    void shouldPushReminderTaskWithCorrectLoanId() throws Exception {
        mockMvc.perform(post("/notify/42").with(csrf()));

        verify(taskQueueService, times(1)).push(
                new NotificationTask(NotificationTask.Type.REMINDER, 42L));
    }

    // --- Routing edge cases ---

    @Test
    @WithMockUser(roles = "BIBLIOTHEEKBEHEERDER")
    void shouldReturnNotFoundWhenLoanIdIsMissing() throws Exception {
        mockMvc.perform(post("/notify").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "BIBLIOTHEEKBEHEERDER")
    void shouldReturnBadRequestWhenLoanIdIsNonNumeric() throws Exception {
        mockMvc.perform(post("/notify/abc").with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "BIBLIOTHEEKBEHEERDER")
    void shouldNotPushTaskWhenLoanIdIsNonNumeric() throws Exception {
        mockMvc.perform(post("/notify/abc").with(csrf()));

        verify(taskQueueService, never()).push(any(NotificationTask.class));
    }
}