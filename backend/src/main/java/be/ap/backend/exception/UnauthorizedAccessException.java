package be.ap.backend.exception;

/**
 * Thrown when a user attempts to access a resource they are not authorised to
 * use (403 Forbidden).
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}