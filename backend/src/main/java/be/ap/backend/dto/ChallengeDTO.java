package be.ap.backend.dto;

import lombok.Data;

/**
 * Data transfer object representing a gamification challenge.
 *
 * <p>
 * Contains the challenge definition (description, condition type, condition
 * value)
 * and whether the current user has completed it.
 * </p>
 */
@Data
public class ChallengeDTO {
    private Long id;
    private String description;
    private String conditionType;
    private String conditionValue;
    private boolean completed;
}