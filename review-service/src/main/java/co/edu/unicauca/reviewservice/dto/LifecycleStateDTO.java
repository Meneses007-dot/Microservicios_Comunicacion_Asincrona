package co.edu.unicauca.reviewservice.dto;

/** Estado que devuelve Lifecycle (GET /lifecycle/{questionId}). */
public record LifecycleStateDTO(Long questionId, String state, String changedBy, String comment) {
}
