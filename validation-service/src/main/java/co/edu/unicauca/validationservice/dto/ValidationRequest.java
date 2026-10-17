package co.edu.unicauca.validationservice.dto;

import java.util.List;

/**
 * Cuerpo de entrada de {@code POST /validate}.
 * Debe coincidir exactamente con el que reutiliza Question Service.
 */
public record ValidationRequest(
        String text,
        List<OptionDTO> options,
        String correctOptionId) {
}
