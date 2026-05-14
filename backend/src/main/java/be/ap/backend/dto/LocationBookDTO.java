package be.ap.backend.dto;

import lombok.Data;

@Data
public class LocationBookDTO {
    private Long id;
    private Long locationId;
    private Long bookId;
    private Integer amount;
    private Integer currentAmount;
    private String note;
}
