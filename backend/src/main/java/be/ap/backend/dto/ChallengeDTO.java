package be.ap.backend.dto;

import lombok.Data;

@Data
public class ChallengeDTO {
    private Long id;
    private String description;
    private String conditionType;
    private String conditionValue;
    private boolean completed;
}