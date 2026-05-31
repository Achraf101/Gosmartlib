// package be.ap.backend.controller;

// import be.ap.backend.entity.School;
// import be.ap.backend.repository.SchoolRepository;
// import be.ap.backend.service.SmartschoolSyncService;
// import jakarta.servlet.ServletException;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.setup.MockMvcBuilders;

// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertInstanceOf;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.Mockito.*;
// import static
// org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static
// org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @ExtendWith(MockitoExtension.class)
// class SmartschoolSyncControllerTest {

// @Mock
// private SmartschoolSyncService syncService;

// @Mock
// private SchoolRepository schoolRepository;

// @InjectMocks
// private SmartschoolSyncController controller;

// private MockMvc mockMvc;

// @BeforeEach
// void setUp() {
// mockMvc = MockMvcBuilders
// .standaloneSetup(controller)
// .build();
// }

// // --- Helper ---

// private School buildSchool(Long id, String clientId, String clientSecret,
// String subdomain) {
// School school = new School();
// school.setId(id);
// school.setOneRosterClientId(clientId);
// school.setOneRosterClientSecret(clientSecret);
// school.setSsSubdomain(subdomain);
// return school;
// }

// // --- Tests ---

// @Test
// void sync_schoolNotFound_throwsRuntimeException() {
// when(schoolRepository.findById(99L)).thenReturn(Optional.empty());

// assertThrows(
// jakarta.servlet.ServletException.class,
// () -> mockMvc.perform(post("/smartschool/sync/99")));

// verifyNoInteractions(syncService);
// }

// @Test
// void sync_missingClientId_returns400() throws Exception {
// School school = buildSchool(1L, null, "secret", "school-a");
// when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));

// mockMvc.perform(post("/smartschool/sync/1"))
// .andExpect(status().isBadRequest())
// .andExpect(content().string("School has no OneRoster credentials
// configured"));

// verifyNoInteractions(syncService);
// }

// @Test
// void sync_missingClientSecret_returns400() throws Exception {
// School school = buildSchool(1L, "clientId", null, "school-a");
// when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));

// mockMvc.perform(post("/smartschool/sync/1"))
// .andExpect(status().isBadRequest())
// .andExpect(content().string("School has no OneRoster credentials
// configured"));

// verifyNoInteractions(syncService);
// }

// @Test
// void sync_bothCredentialsMissing_returns400() throws Exception {
// School school = buildSchool(1L, null, null, "school-a");
// when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));

// mockMvc.perform(post("/smartschool/sync/1"))
// .andExpect(status().isBadRequest())
// .andExpect(content().string("School has no OneRoster credentials
// configured"));

// verifyNoInteractions(syncService);
// }

// @Test
// void sync_validSchool_returns200AndCallsService() throws Exception {
// School school = buildSchool(1L, "clientId", "secret", "school-a");
// when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));

// mockMvc.perform(post("/smartschool/sync/1"))
// .andExpect(status().isOk())
// .andExpect(content().string("Sync completed for school: school-a"));

// verify(syncService, times(1)).syncSchool(school);
// }

// @Test
// void sync_serviceThrowsException_propagatesAsServletException() {
// School school = buildSchool(1L, "clientId", "secret", "school-a");
// when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
// doThrow(new RuntimeException("Sync
// failed")).when(syncService).syncSchool(school);

// ServletException ex = assertThrows(
// jakarta.servlet.ServletException.class,
// () -> mockMvc.perform(post("/smartschool/sync/1")));

// assertInstanceOf(RuntimeException.class, ex.getCause());
// assertEquals("Sync failed", ex.getCause().getMessage());
// verify(syncService, times(1)).syncSchool(school);
// }
// }