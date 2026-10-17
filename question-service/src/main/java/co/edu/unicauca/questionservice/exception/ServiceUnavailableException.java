package co.edu.unicauca.questionservice.exception;

/** Fallo de negocio o de infraestructura al hablar con otro servicio (se traduce a 503). */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
