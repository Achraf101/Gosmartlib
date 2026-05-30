package be.ap.backend.exception;

/**
 * Exception die wordt gegooid wanneer encryptie- of decryptieoperaties falen.
 *
 * <p>
 * Wordt gebruikt om onderliggende cryptografische fouten te encapsuleren
 * zonder implementatiedetails naar hogere lagen bloot te stellen.
 * </p>
 */
public class EncryptionException extends RuntimeException {
    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}