package co.edu.unicauca.reviewservice.dto;

/** Cuerpo enviado a Lifecycle (POST /lifecycle/transition). */
public record TransitionPayload(Long questionId, String targetState, String requestedBy, String comment) {
}
