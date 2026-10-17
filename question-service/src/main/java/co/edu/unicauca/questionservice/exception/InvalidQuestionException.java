package co.edu.unicauca.questionservice.exception;

import java.util.List;

/** La validación síncrona falló: la pregunta no se persiste (se traduce a 400). */
public class InvalidQuestionException extends RuntimeException {

    private final List<String> errors;

    public InvalidQuestionException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
