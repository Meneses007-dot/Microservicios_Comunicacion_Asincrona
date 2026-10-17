package co.edu.unicauca.reviewservice.dto;

/** Respuesta de una asignación exitosa (201). */
public record AssignResponse(Long assignmentId, Long questionId, Long reviewerId, String status) {
}
