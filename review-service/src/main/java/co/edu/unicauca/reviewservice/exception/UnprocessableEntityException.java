package co.edu.unicauca.reviewservice.exception;

/** Revisor sin el rol requerido (se traduce a 422). */
public class UnprocessableEntityException extends RuntimeException {

    public UnprocessableEntityException(String message) {
        super(message);
    }
}
