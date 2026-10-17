package co.edu.unicauca.questionservice.dto;

/** Respuesta de {@code POST /questions} (201). */
public record CreateQuestionResponse(Long id, String status, String message) {
}
