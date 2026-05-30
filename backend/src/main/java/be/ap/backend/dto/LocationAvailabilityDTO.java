package be.ap.backend.dto;

import lombok.Data;

@Data
public class LocationAvailabilityDTO {
    private Long locationBookId;
    private Long locationId;
    private String locationName;
    private int amount;
    private int currentAmount;
    private int damagedCount;
    private int notedCount;
}
