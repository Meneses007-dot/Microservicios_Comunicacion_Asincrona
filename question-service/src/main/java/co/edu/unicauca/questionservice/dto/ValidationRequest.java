package co.edu.unicauca.questionservice.dto;

import java.util.List;

/** Cuerpo enviado a Validation Service (POST /validate). */
public record ValidationRequest(String text, List<OptionDTO> options, String correctOptionId) {
}
