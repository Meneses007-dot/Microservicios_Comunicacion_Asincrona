package co.edu.unicauca.questionservice.dto;

import java.util.List;

/** Respuesta de Validation Service (duplicada por servicio). */
public record ValidationResult(boolean valid, List<String> errors) {
}
