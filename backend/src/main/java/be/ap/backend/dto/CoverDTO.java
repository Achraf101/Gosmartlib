package be.ap.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data transfer object representing the uploaded or assigned cover image
 * for a specific book.
 */
@Data
@AllArgsConstructor
public class CoverDTO {
    public Long bookId;
    public String cover;
}
