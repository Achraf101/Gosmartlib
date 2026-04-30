package be.ap.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Long id;
    private Long bookId;

    @NotNull
    @Min(1)
    @Max(5)
    private double rating;

    @Size(max = 500)
    private String content;

    private LocalDateTime added;
}