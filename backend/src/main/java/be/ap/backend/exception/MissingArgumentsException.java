package be.ap.backend.exception;

public class MissingArgumentsException extends RuntimeException {
    public MissingArgumentsException(String message) {
        super(message);
    }
}
