package be.ap.backend.dto;

import be.ap.backend.enums.LoanStatus;

public record UpdateStatusDTO(LoanStatus status) {
}
