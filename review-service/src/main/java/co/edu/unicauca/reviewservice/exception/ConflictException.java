package co.edu.unicauca.reviewservice.exception;

/** Conflicto de estado (se traduce a 409). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
