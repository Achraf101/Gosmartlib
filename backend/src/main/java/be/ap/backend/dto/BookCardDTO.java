package be.ap.backend.dto;

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
    private String author_name;

}
