package be.ap.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OneRosterCredentials {
    private String clientId;
    private String clientSecret;
}