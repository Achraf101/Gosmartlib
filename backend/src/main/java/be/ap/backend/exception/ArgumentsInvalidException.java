package be.ap.backend.exception;

public class ArgumentsInvalidException extends RuntimeException {
    public ArgumentsInvalidException(String message) {
        super(message);
    }
}
