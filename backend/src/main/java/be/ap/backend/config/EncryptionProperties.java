package be.ap.backend.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuratie-eigenschappen voor encryptie binnen de applicatie.
 * 
 * @param key     de encryptiesleutel
 * @param version de versie van de gebruikte encryptieconfiguratie
 */
@ConfigurationProperties(prefix = "app.encryption")
@Validated
public record EncryptionProperties(
        @NotBlank String key,
        int version) {
}