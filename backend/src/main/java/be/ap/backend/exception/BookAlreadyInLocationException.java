package be.ap.backend.exception;

public class BookAlreadyInLocationException extends RuntimeException {
    public BookAlreadyInLocationException(String message) {
        super(message);
    }
}
