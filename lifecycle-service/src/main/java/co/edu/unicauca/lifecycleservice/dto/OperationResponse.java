package co.edu.unicauca.lifecycleservice.dto;

import co.edu.unicauca.lifecycleservice.model.OperationStatus;

import java.time.Instant;

/** Resultado de una operación asíncrona (GET /lifecycle/operations/{operationId}). */
public record OperationResponse(
        String operationId,
        Long questionId,
        String type,
        OperationStatus status,
        String message,
        Instant createdAt,
        Instant completedAt) {
}
