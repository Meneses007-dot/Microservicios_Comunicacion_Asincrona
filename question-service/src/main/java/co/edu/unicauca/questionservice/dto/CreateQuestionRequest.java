package co.edu.unicauca.questionservice.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Cuerpo de {@code POST /questions} y de {@code PUT /questions/{id}}.
 * Es el mismo de /validate más {@code authorId}.
 */
public record CreateQuestionRequest(
        String title,
        @NotBlank(message = "text es obligatorio") String text,
        List<OptionDTO> options,
        String correctOptionId,
        Long authorId) {
}
