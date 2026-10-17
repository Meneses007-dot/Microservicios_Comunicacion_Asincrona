package co.edu.unicauca.reviewservice.dto;

/** Respuesta del registro de un resultado de revisión. */
public record ResultResponse(Long reviewId, Long questionId, String result, String status) {
}
