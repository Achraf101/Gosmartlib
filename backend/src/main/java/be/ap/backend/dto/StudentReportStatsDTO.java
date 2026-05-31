package be.ap.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing aggregated reading statistics for a student,
 * including genre preference, average rating, and return punctuality.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentReportStatsDTO {
    @JsonProperty("favoriteGenre")
    private String favoriteGenre;
    @JsonProperty("averageRating")
    private Double averageRating;
    @JsonProperty("punctualityRate")
    private Double punctualityRate;
    @JsonProperty("onTimeReturns")
    private int onTimeReturns;
    @JsonProperty("totalReturns")
    private int totalReturns;
}
