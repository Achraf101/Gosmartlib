package be.ap.backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentReviewDTO {
    private Long id;
    @JsonProperty("bookId")
    private Long bookId;
    @JsonProperty("bookTitle")
    private String bookTitle;
    @JsonProperty("bookCover")
    private String bookCover;
    private double rating;
    private String content;
    private LocalDateTime added;
}
