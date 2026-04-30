package be.ap.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import be.ap.backend.entity.Author;
import lombok.Data;

@Data
public class LoanBookDTO {
    private Long id;
    @JsonProperty("bookId")
    private Long bookId;
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
