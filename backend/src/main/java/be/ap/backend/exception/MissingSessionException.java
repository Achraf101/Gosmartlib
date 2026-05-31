package be.ap.backend.exception;

/**
 * Thrown when a required user session is absent or not properly initialised.
 */
public class MissingSessionException extends RuntimeException {
    public MissingSessionException(String message) {
        super(message);
    }
}