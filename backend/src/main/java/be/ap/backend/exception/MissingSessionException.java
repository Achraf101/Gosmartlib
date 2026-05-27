package be.ap.backend.exception;

public class MissingSessionException extends RuntimeException {
    public MissingSessionException(String message) {
        super(message);
    }
}