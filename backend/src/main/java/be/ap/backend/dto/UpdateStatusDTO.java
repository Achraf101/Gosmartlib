package be.ap.backend.dto;

import be.ap.backend.enums.LoanStatus;

/**
 * DTO carrying an updated loan status value.
 */
public record UpdateStatusDTO(LoanStatus status) {
}
