package co.edu.unicauca.questionservice.dto;

/**
 * Estado de una operación asíncrona devuelto a través de
 * {@code GET /operations/{operationId}}. El status es
 * PENDING | COMPLETED | FAILED (según lo que reporte Lifecycle).
 */
public record OperationResponse(
        String operationId,
        Long questionId,
        String type,
        String status,
        String message) {
}
