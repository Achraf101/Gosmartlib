package be.ap.backend.exception;

/**
 * Exception die wordt gegooid wanneer verplichte argumenten ontbreken in een
 * request of operatie.
 *
 * <p>
 * Wordt gebruikt om te signaleren dat een actie niet kan worden uitgevoerd
 * omdat essentiële inputgegevens ontbreken.
 * </p>
 */
public class MissingArgumentsException extends RuntimeException {
    public MissingArgumentsException(String message) {
        super(message);
    }
}
