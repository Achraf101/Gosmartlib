package be.ap.backend.dto;

import java.time.LocalDateTime;

import be.ap.backend.entity.Author;

public record BookmarkedDTO (Long id,
    Long bookId,
    String title,
    String cover,
    Author author,
    LocalDateTime added
) {}
