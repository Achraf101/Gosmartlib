package be.ap.backend.controller;

import be.ap.backend.queue.TaskQueueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskQueueService taskQueueService;

    // @Test
    // @WithMockUser(roles = "BIBLIOTHEEKBEHEERDER")
    // void shouldReturnTrueWhenLoanIdIsValid() throws Exception {
    // mockMvc.perform(post("/notify/1"))
    // .andExpect(status().isOk())
    // .andExpect(content().string("true"));
    // }

    // @Test
    // @WithMockUser(roles = "BIBLIOTHEEKBEHEERDER")
    // void shouldPushTaskWhenLoanIdIsValid() throws Exception {
    // mockMvc.perform(post("/notify/1"));

    // verify(taskQueueService, times(1)).push(any(NotificationTask.class));
    // }

    @Test
    void shouldReturnUnauthorizedWhenNoRole() throws Exception {
        mockMvc.perform(post("/notify/1"))
                .andExpect(status().isForbidden());
    }
}