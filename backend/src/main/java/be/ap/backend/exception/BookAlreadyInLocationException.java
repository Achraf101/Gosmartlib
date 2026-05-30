package be.ap.backend.exception;

/**
 * Exception die aangeeft dat een boek al aanwezig is op de opgegeven locatie.
 *
 * <p>
 * Wordt gebruikt om dubbele plaatsingen van boeken te verhinderen binnen
 * dezelfde locatiecontext.
 * </p>
 */
public class BookAlreadyInLocationException extends RuntimeException {
    public BookAlreadyInLocationException(String message) {
        super(message);
    }
}
