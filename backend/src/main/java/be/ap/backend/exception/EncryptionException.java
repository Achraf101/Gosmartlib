package be.ap.backend.exception;

/**
 * Thrown when an encryption or decryption operation fails.
 */
public class EncryptionException extends RuntimeException {
    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}