package be.ap.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.RequiredArgsConstructor;

/**
 * Entry point van de backend Spring Boot applicatie.
 *
 * <p>
 * Start de Spring context en bootstrap van alle configuratie,
 * beans en componenten.
 * </p>
 */
@SpringBootApplication
@RequiredArgsConstructor
@EnableScheduling
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}