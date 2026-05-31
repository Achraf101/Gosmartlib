package be.ap.backend.dto;

/**
 * DTO representing a frequently borrowed book and its borrow count.
 */
public record TopBookDTO(String title, int count) {
}
