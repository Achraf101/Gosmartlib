package be.ap.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a book theme.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThemeDTO {
    private Long id;
    private String name;
}