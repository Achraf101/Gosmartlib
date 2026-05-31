package be.ap.backend.dto;

/**
 * DTO representing a school's book inventory statistics.
 */
public record SchoolStatsDTO(int totalBooks, int availableBooks) {
}
