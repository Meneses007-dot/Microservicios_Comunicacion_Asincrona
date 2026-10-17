package co.edu.unicauca.lifecycleservice.exception;

/** Recurso inexistente (se traduce a 404). */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
