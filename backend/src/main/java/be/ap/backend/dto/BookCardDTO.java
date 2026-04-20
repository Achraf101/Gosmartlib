package be.ap.backend.dto;

import be.ap.backend.entity.Author;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCardDTO {
    private Long id;
    private String title;
    private String cover;
    private Author author;

    public BookCardDTO(Long id, String title, String cover, Author author) {
        this.id = id;
        this.title = title;
        this.cover = cover;
        this.author = author;
    }

}
