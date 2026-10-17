package co.edu.unicauca.lifecycleservice.dto;

import co.edu.unicauca.lifecycleservice.model.QuestionStatus;

/** Respuesta de una transición exitosa. */
public record TransitionResponse(Long questionId, QuestionStatus previousState, QuestionStatus currentState) {
}
