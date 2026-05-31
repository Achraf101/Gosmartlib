package be.ap.backend.exception;

/**
 * Thrown when one or more provided arguments fail validation.
 */
public class ArgumentsInvalidException extends RuntimeException {
    public ArgumentsInvalidException(String message) {
        super(message);
    }
}
