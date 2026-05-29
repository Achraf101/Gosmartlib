package be.ap.backend.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowedBookPreviewDTO {
    @JsonProperty("bookId")
    private Long bookId;
    private String title;
    private String author;
    private String cover;
    private LocalDate start;
    private LocalDate end;
    private boolean returned;
    private boolean overdue;
    private boolean requested;
    private boolean accepted;
    private boolean received;
    private boolean declined;
}
