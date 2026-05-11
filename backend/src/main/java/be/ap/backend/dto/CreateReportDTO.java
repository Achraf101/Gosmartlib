package be.ap.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateReportDTO {
    @Size(max = 500)
    private String note;
}
