package be.ap.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCardDTO {
    private Long id;
    private String title;
    private String cover;
    private String author_name;

    public BookCardDTO(Long id, String title, String cover, String author_name) {
        this.id = id;
        this.title = title;
        this.cover = cover;
        this.author_name = author_name;
    }

}
