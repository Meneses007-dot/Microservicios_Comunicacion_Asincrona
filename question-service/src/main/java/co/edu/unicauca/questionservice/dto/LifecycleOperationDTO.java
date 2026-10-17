package co.edu.unicauca.questionservice.dto;

import java.time.Instant;

/** Resultado de operación tal como lo devuelve Lifecycle (duplicado por servicio). */
public record LifecycleOperationDTO(
        String operationId,
        Long questionId,
        String type,
        String status,
        String message,
        Instant createdAt,
        Instant completedAt) {
}
