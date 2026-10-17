package co.edu.unicauca.questionservice.exception;

/** La pregunta no existe (se traduce a 404). */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
