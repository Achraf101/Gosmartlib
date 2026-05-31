package be.ap.backend.exception;

/**
 * Thrown when required arguments are missing from a request or operation.
 */
public class MissingArgumentsException extends RuntimeException {
    public MissingArgumentsException(String message) {
        super(message);
    }
}
