package be.ap.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import be.ap.backend.entity.Author;
import lombok.Data;

/**
 * DTO representing a book entry within a loan, including copy tracking and
 * amounts.
 */
@Data
public class LoanBookDTO {
    private Long id;
    @JsonProperty("bookId")
    private Long bookId;
    @JsonProperty("bookCopyId")
    private Long bookCopyId;
    @JsonProperty("requestedAmount")
    private Integer requestedAmount;
    @JsonProperty("receivedAmount")
    private Integer receivedAmount;
    @JsonProperty("returnedAmount")
    private Integer returnedAmount;
    @JsonProperty("bookTitle")
    private String bookTitle;
    private String cover;
    private Author author;
}
