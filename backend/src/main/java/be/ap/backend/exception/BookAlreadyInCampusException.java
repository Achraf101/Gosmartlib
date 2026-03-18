package be.ap.backend.exception;

public class BookAlreadyInCampusException extends RuntimeException {
    public BookAlreadyInCampusException(String message) {
        super(message);
    }
}
