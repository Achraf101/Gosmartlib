package be.ap.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
