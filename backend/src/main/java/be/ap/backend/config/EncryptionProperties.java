package be.ap.backend.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for application-level encryption settings.
 *
 * <p>
 * Bound to properties prefixed with {@code app.encryption}.
 * </p>
 */
@ConfigurationProperties(prefix = "app.encryption")
@Validated
public record EncryptionProperties(
        @NotBlank String key,
        int version) {
}