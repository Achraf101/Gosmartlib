package be.ap.backend.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.encryption")
@Validated
public record EncryptionProperties(
        @NotBlank String key,
        int version) {
}