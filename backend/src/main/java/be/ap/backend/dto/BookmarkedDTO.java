package be.ap.backend.dto;

import java.time.LocalDateTime;

public record BookmarkedDTO (Long id,
    Long bookId,
    String title,
    String cover,
    String authorName,
    LocalDateTime added
) {}
