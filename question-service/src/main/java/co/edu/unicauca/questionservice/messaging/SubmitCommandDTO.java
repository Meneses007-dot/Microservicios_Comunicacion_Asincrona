package co.edu.unicauca.questionservice.messaging;

import java.time.Instant;

/**
 * Comando SubmitQuestionForReview: lo publica Question Service y lo consume
 * Lifecycle Service. Se duplica la clase en cada servicio a propósito.
 */
public record SubmitCommandDTO(
        Long questionId,
        String operationId,
        String requestedBy,
        Instant requestedAt) {
}
