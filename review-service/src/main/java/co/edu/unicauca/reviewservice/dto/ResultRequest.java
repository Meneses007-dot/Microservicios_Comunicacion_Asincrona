package co.edu.unicauca.reviewservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de {@code POST /reviews/{questionId}/result}.
 *
 * @param result APPROVED o REJECTED
 */
public record ResultRequest(
        @NotNull(message = "reviewerId es obligatorio") Long reviewerId,
        @NotBlank(message = "result es obligatorio") String result,
        String comments) {
}
