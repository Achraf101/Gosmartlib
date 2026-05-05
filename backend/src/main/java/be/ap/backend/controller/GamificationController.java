package be.ap.backend.controller;

import be.ap.backend.dto.ChallengeDTO;
import be.ap.backend.dto.GamificationDTO;
import be.ap.backend.entity.UserChallenge;
import be.ap.backend.service.GamificationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;

    @GetMapping
    public ResponseEntity<GamificationDTO> getGamification(HttpSession session) {
        Object raw = session.getAttribute("userId");
        Long userId = (raw != null) ? Long.valueOf(raw.toString()) : null;

        int totalBooks = gamificationService.getTotalBooks(userId);
        String streakLevel = gamificationService.getStreakLevel(totalBooks);
        List<UserChallenge> userChallenges = gamificationService.getChallengesForUser(userId);

        gamificationService.checkChallenges(userId);

        List<ChallengeDTO> challengeDTOs = userChallenges.stream().map(uc -> {
            ChallengeDTO dto = new ChallengeDTO();
            dto.setId(uc.getId());
            dto.setDescription(uc.getChallenge().getDescription());
            dto.setConditionType(uc.getChallenge().getConditionType());
            dto.setConditionValue(uc.getChallenge().getConditionValue());
            dto.setCompleted(uc.isCompleted());
            return dto;
        }).toList();

        GamificationDTO result = new GamificationDTO();
        result.setTotalBooks(totalBooks);
        result.setStreakLevel(streakLevel);
        result.setChallenges(challengeDTOs);

        return ResponseEntity.ok(result);
    }
}