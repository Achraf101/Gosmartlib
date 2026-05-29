package be.ap.backend.dto;

import java.util.Set;

import be.ap.backend.entity.School;
import be.ap.backend.entity.UserRole;
import be.ap.backend.entity.Loan;
import lombok.Data;

@Data
public class LoanLookupContextDTO {
    private Loan loan;
    private String oneRosterId;
    private School school;
    private Set<UserRole> roles;
}
