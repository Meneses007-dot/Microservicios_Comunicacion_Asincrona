package co.edu.unicauca.reviewservice.exception;

/** Un servicio externo no respondió (se traduce a 503). */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
