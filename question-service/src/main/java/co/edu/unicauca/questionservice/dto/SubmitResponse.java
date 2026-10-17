package co.edu.unicauca.questionservice.dto;

/** Respuesta inmediata (202 Accepted) al solicitar el envío a revisión. */
public record SubmitResponse(String message, String operationId, String status) {
}
