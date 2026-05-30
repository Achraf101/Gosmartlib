package be.ap.backend.exception;

/**
 * Exception die wordt gegooid wanneer een gebruiker probeert toegang te krijgen
 * tot een resource
 * waarvoor hij niet de vereiste rechten heeft.
 *
 * <p>
 * Wordt gebruikt voor autorisatie-fouten (403 Forbidden) en staat los van
 * authenticatieproblemen (niet ingelogd).
 * </p>
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}