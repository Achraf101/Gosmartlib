package be.ap.backend.exception;

/**
 * Thrown when a book is already assigned to the given location.
 */
public class BookAlreadyInLocationException extends RuntimeException {
    public BookAlreadyInLocationException(String message) {
        super(message);
    }
}
