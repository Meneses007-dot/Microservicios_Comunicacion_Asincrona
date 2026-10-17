package co.edu.unicauca.validationservice.dto;

import java.util.List;

/**
 * Resultado de la validación.
 *
 * @param valid  true si no hay errores
 * @param errors lista de motivos; vacía cuando {@code valid} es true
 */
public record ValidationResult(boolean valid, List<String> errors) {
}
