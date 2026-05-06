package be.ap.backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentReportDTO {
    @JsonProperty("studentId")
    private Long studentId;
    @JsonProperty("studentName")
    private String studentName;
    @JsonProperty("studentUsername")
    private String studentUsername;
    @JsonProperty("borrowCounts")
    private BorrowCountsDTO borrowCounts;
    @JsonProperty("borrowedBooksPreview")
    private List<BorrowedBookPreviewDTO> borrowedBooksPreview;
    private List<StudentReviewDTO> reviews;
    private StudentReportStatsDTO stats;
}
