package be.ap.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CoverDTO {
    public Long bookId;
    public String cover;

}
