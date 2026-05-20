package be.ap.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class GamificationDTO {
    private int totalBooks;
    private String streakLevel;
    private List<ChallengeDTO> challenges;
}