package be.ap.backend.exception;

/**
 * Exception die wordt gegooid wanneer vereiste sessie-informatie ontbreekt.
 *
 * <p>
 * Wordt gebruikt wanneer operaties afhankelijk zijn van een actieve
 * gebruikerssessie,
 * maar die sessie niet beschikbaar is of niet correct geïnitialiseerd werd.
 * </p>
 */
public class MissingSessionException extends RuntimeException {
    public MissingSessionException(String message) {
        super(message);
    }
}