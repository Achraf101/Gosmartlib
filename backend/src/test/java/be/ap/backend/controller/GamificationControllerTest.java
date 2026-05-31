// package be.ap.backend.controller;

// import be.ap.backend.dto.ChallengeDTO;
// import be.ap.backend.dto.GamificationDTO;
// import be.ap.backend.entity.Challenge;
// import be.ap.backend.entity.UserChallenge;
// import be.ap.backend.service.GamificationService;
// import jakarta.servlet.http.HttpSession;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.http.ResponseEntity;

// import java.time.LocalDate;
// import java.time.format.DateTimeFormatter;
// import java.util.List;

// import static org.mockito.Mockito.*;
// import static org.junit.jupiter.api.Assertions.*;

// @ExtendWith(MockitoExtension.class)
// class GamificationControllerTest {

// @Mock
// private GamificationService gamificationService;

// @Mock
// private HttpSession session;

// @InjectMocks
// private GamificationController gamificationController;

// private final Long USER_ID = 1L;
// private final String CURRENT_MONTH =
// LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

// private UserChallenge buildUserChallenge(String conditionType, String
// conditionValue, boolean completed) {
// Challenge challenge = new Challenge();
// challenge.setDescription("Test challenge");
// challenge.setConditionType(conditionType);
// challenge.setConditionValue(conditionValue);

// UserChallenge uc = new UserChallenge();
// uc.setId(1L);
// uc.setUserId(USER_ID);
// uc.setChallenge(challenge);
// uc.setMonth(CURRENT_MONTH);
// uc.setCompleted(completed);
// uc.setAssignedAt(LocalDate.now());
// return uc;
// }

// @Test
// void getGamification_correcteData_wordtTeruggegeven() {
// UserChallenge uc = buildUserChallenge("language", "fr", false);

// when(session.getAttribute("userId")).thenReturn(USER_ID);
// when(gamificationService.getTotalBooks(USER_ID)).thenReturn(5);
// when(gamificationService.getStreakLevel(5)).thenReturn("5 op rij");
// when(gamificationService.getChallengesForUser(USER_ID)).thenReturn(List.of(uc));

// ResponseEntity<GamificationDTO> response =
// gamificationController.getGamification(session);

// assertEquals(200, response.getStatusCode().value());
// assertNotNull(response.getBody());
// assertEquals(5, response.getBody().getTotalBooks());
// assertEquals("5 op rij", response.getBody().getStreakLevel());
// assertEquals(1, response.getBody().getChallenges().size());
// }

// @Test
// void getGamification_challengeDTO_correctGemapped() {
// UserChallenge uc = buildUserChallenge("genre", "Fantasy", true);

// when(session.getAttribute("userId")).thenReturn(USER_ID);
// when(gamificationService.getTotalBooks(USER_ID)).thenReturn(3);
// when(gamificationService.getStreakLevel(3)).thenReturn("Op dreef");
// when(gamificationService.getChallengesForUser(USER_ID)).thenReturn(List.of(uc));

// ResponseEntity<GamificationDTO> response =
// gamificationController.getGamification(session);

// ChallengeDTO dto = response.getBody().getChallenges().get(0);
// assertEquals("genre", dto.getConditionType());
// assertEquals("Fantasy", dto.getConditionValue());
// assertTrue(dto.isCompleted());
// }

// @Test
// void getGamification_checkChallengesWordtAangeroepen() {
// when(session.getAttribute("userId")).thenReturn(USER_ID);
// when(gamificationService.getTotalBooks(USER_ID)).thenReturn(0);
// when(gamificationService.getStreakLevel(0)).thenReturn("Geen level");
// when(gamificationService.getChallengesForUser(USER_ID)).thenReturn(List.of());

// gamificationController.getGamification(session);

// verify(gamificationService).checkChallenges(USER_ID);
// }

// @Test
// void getGamification_geenUserId_inSessie_nullWordtDoorgegeven() {
// when(session.getAttribute("userId")).thenReturn(null);
// when(gamificationService.getTotalBooks(null)).thenReturn(0);
// when(gamificationService.getStreakLevel(0)).thenReturn("Geen level");
// when(gamificationService.getChallengesForUser(null)).thenReturn(List.of());

// ResponseEntity<GamificationDTO> response =
// gamificationController.getGamification(session);

// assertEquals(200, response.getStatusCode().value());
// verify(gamificationService).getTotalBooks(null);
// }

// @Test
// void getGamification_geenChallenges_legeListWordtTeruggegeven() {
// when(session.getAttribute("userId")).thenReturn(USER_ID);
// when(gamificationService.getTotalBooks(USER_ID)).thenReturn(0);
// when(gamificationService.getStreakLevel(0)).thenReturn("Geen level");
// when(gamificationService.getChallengesForUser(USER_ID)).thenReturn(List.of());

// ResponseEntity<GamificationDTO> response =
// gamificationController.getGamification(session);

// assertEquals(0, response.getBody().getChallenges().size());
// }

// @Test
// void getGamification_drieChalllenges_allesDrieTeruggegeven() {
// UserChallenge uc1 = buildUserChallenge("language", "fr", false);
// UserChallenge uc2 = buildUserChallenge("genre", "Fantasy", true);
// UserChallenge uc3 = buildUserChallenge("pages", "500", false);

// when(session.getAttribute("userId")).thenReturn(USER_ID);
// when(gamificationService.getTotalBooks(USER_ID)).thenReturn(10);
// when(gamificationService.getStreakLevel(10)).thenReturn("Nachtlezer");
// when(gamificationService.getChallengesForUser(USER_ID)).thenReturn(List.of(uc1,
// uc2, uc3));

// ResponseEntity<GamificationDTO> response =
// gamificationController.getGamification(session);

// assertEquals(3, response.getBody().getChallenges().size());
// }
// }