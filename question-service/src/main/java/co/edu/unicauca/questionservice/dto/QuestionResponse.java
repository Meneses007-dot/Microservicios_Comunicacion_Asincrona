package co.edu.unicauca.questionservice.dto;

import java.time.Instant;
import java.util.List;

/** Representación de una pregunta devuelta al cliente. */
public record QuestionResponse(
        Long id,
        String title,
        String text,
        Long authorId,
        String status,
        List<OptionDTO> options,
        String correctOptionId,
        Instant createdAt,
        Instant updatedAt) {
}
