package be.ap.backend.dto;

import lombok.Data;

@Data
public class CampusBookDTO {
    private Long campusId;
    private Long bookId;
    private Integer amount;
    private Integer currentAmount;
    private String location;
}
