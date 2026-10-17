package co.edu.unicauca.lifecycleservice.messaging;

import java.time.Instant;

/**
 * Comando SubmitQuestionForReview (nombre lógico) enviado por Question Service
 * y consumido por Lifecycle Service. Mismo JSON en ambos lados; la clase se
 * duplica deliberadamente por servicio.
 */
public record SubmitCommandDTO(
        Long questionId,
        String operationId,
        String requestedBy,
        Instant requestedAt) {
}
