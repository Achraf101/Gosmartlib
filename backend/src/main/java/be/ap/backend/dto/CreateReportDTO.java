package be.ap.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data transfer object used to submit a report for a review.
 *
 * <p>
 * Contains an optional note explaining the reason for the report.
 * </p>
 */
@Data
public class CreateReportDTO {
    @Size(max = 500)
    private String note;
}
