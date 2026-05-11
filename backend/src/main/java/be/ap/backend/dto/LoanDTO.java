package be.ap.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import be.ap.backend.entity.LoanStatus;
import lombok.Data;

@Data
public class LoanDTO {
    private Long id;
    @JsonProperty("userId")
    private Long userId;
    private String username;
    @JsonProperty("campusId")
    private Long campusId;
    private Byte extended;
    private LocalDate start;
    private LocalDate end;
    private String note;
    private LoanStatus status;
    private Boolean closed;
    private LoanBookDTO[] books;
    private LocalDateTime created;
    @JsonProperty("groupId")
    private String groupId;
}