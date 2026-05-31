package be.ap.backend.dto;

import lombok.Data;

/**
 * DTO representing the stock of a book at a specific location.
 */
@Data
public class LocationBookDTO {
    private Long id;
    private Long locationId;
    private Long bookId;
    private Integer amount;
    private Integer currentAmount;
}
