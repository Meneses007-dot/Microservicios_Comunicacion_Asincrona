package co.edu.unicauca.lifecycleservice.exception;

import co.edu.unicauca.lifecycleservice.model.QuestionStatus;

/** Transición no permitida por la máquina de estados (se traduce a 409 Conflict). */
public class InvalidTransitionException extends RuntimeException {

    public InvalidTransitionException(QuestionStatus from, QuestionStatus to) {
        super("Transición no permitida: " + from + " \u2192 " + to);
    }

    public InvalidTransitionException(String message) {
        super(message);
    }
}
