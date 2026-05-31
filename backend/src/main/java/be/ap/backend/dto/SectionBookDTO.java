package be.ap.backend.dto;

import be.ap.backend.entity.Book;

/**
 * DTO pairing a book with its ranking position within a section.
 */
public record SectionBookDTO(short ranking, Book book) {
}
