package be.ap.backend.dto;

import be.ap.backend.entity.Author;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
public class BookCardDTO {
    private Long id;
    private String title;
    private String cover;
    private Author author;

}
