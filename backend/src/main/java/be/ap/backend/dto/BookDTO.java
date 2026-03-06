package be.ap.backend.dto;

import be.ap.backend.entity.Contributor;
import be.ap.backend.entity.Genre;
import be.ap.backend.entity.Language;
import be.ap.backend.entity.Publisher;
import be.ap.backend.entity.School;
import be.ap.backend.entity.Serie;

public record BookDTO(    
    String isbn,
    String title,
    Serie series,
    int series_count,
    Contributor[] contributors,
    Publisher publisher,
    Genre genre,
    String description,
    boolean fiction,
    int published,
    String cover,
    Language language,
    int age_start,
    int age_end,
    int pages,
    String font_size,
    int rating_total,
    int rating_count,
    School school_id,
    String added) {
}
