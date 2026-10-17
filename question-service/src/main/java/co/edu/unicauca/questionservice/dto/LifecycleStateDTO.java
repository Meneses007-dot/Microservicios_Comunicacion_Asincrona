package co.edu.unicauca.questionservice.dto;

/**
 * Vista reducida del estado que devuelve Lifecycle (GET /lifecycle/{id}).
 * Solo se usa el campo state; el resto se ignora al deserializar.
 */
public record LifecycleStateDTO(Long questionId, String state, String changedBy, String comment) {
}
