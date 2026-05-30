package be.ap.backend.exception;

/**
 * Exception die wordt gegooid wanneer meegegeven argumenten ongeldig of niet
 * valideerbaar zijn.
 *
 * <p>
 * Wordt gebruikt om validatiefouten expliciet te onderscheiden van andere
 * runtime fouten.
 * </p>
 */
public class ArgumentsInvalidException extends RuntimeException {
    public ArgumentsInvalidException(String message) {
        super(message);
    }
}
