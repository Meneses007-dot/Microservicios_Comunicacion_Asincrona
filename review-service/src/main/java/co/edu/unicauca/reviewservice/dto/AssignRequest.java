package co.edu.unicauca.reviewservice.dto;

import jakarta.validation.constraints.NotNull;

/** Cuerpo de {@code POST /reviews/assign}. */
public record AssignRequest(
        @NotNull(message = "questionId es obligatorio") Long questionId,
        @NotNull(message = "reviewerId es obligatorio") Long reviewerId,
        String assignedBy) {
}
