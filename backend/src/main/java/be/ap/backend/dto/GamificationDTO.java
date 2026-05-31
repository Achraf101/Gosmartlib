package be.ap.backend.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO representing a user's gamification summary,
 * including reading statistics and active challenges.
 */
@Data
public class GamificationDTO {
    private int totalBooks;
    private String streakLevel;
    private List<ChallengeDTO> challenges;
}