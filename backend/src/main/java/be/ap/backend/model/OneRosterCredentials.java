package be.ap.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Credentials used to authenticate against an external OneRoster provider.
 */
@Data
@AllArgsConstructor
public class OneRosterCredentials {
    private String clientId;
    private String clientSecret;
}