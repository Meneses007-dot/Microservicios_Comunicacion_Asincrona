package co.edu.unicauca.lifecycleservice.dto;

import co.edu.unicauca.lifecycleservice.model.QuestionStatus;

import java.time.Instant;

/** Estado actual de una pregunta (GET /lifecycle/{questionId}). */
public record LifecycleStateResponse(
        Long questionId,
        QuestionStatus state,
        Instant changedAt,
        String changedBy,
        String comment) {
}
