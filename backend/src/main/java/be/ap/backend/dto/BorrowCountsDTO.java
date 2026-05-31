package be.ap.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object representing aggregated borrowing statistics
 * over multiple time windows.
 *
 * <p>
 * Used to expose the number of borrows grouped by week, month,
 * semester, and school year for reporting and analytics purposes.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowCountsDTO {
    private int week;
    private int month;
    private int semester;
    @JsonProperty("schoolYear")
    private int schoolYear;
}
