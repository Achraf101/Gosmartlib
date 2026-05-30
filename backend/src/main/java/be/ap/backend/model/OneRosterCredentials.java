package be.ap.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Bevat de authenticatiegegevens voor OneRoster-integratie.
 *
 * <p>
 * Wordt gebruikt om API-calls te authenticeren bij een externe
 * OneRoster-provider.
 * </p>
 */
@Data
@AllArgsConstructor
public class OneRosterCredentials {
    private String clientId;
    private String clientSecret;
}