package co.edu.unicauca.lifecycleservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de {@code POST /lifecycle/transition}.
 * El questionId viaja en el cuerpo (decisión de diseño nº 4).
 */
public record TransitionRequest(
        @NotNull(message = "questionId es obligatorio") Long questionId,
        @NotBlank(message = "targetState es obligatorio") String targetState,
        String requestedBy,
        String comment) {
}
