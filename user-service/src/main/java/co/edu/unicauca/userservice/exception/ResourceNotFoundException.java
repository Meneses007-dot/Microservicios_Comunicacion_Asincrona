package co.edu.unicauca.userservice.exception;

/** Se lanza cuando un recurso solicitado no existe (se traduce a 404). */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
