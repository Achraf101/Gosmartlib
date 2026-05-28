package be.ap.backend.dto;

import lombok.Data;

@Data
public class LocationBookDetailDTO {
    private Long id;
    private Long locationId;
    private Long bookId;
    private String bookTitle;
    private String authorName;
    private String bookCover;
    private Integer amount;
    private Integer currentAmount;
    /** ISBN if the book has one (EAN-13, already printed on the physical book),
     *  otherwise "LB{id}" — used as the scannable barcode identifier. */
    private String barcode;
}