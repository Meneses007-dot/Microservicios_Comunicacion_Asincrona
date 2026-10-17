package co.edu.unicauca.validationservice.service;

import co.edu.unicauca.validationservice.dto.OptionDTO;
import co.edu.unicauca.validationservice.dto.ValidationRequest;
import co.edu.unicauca.validationservice.dto.ValidationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reglas de validación de una pregunta (decisión de diseño nº 7 del instructivo):
 * <ul>
 *   <li>enunciado no vacío;</li>
 *   <li>exactamente N opciones (N configurable, por defecto 4);</li>
 *   <li>ids de opción presentes y únicos;</li>
 *   <li>textos de opción no vacíos y distintos entre sí;</li>
 *   <li>{@code correctOptionId} debe coincidir con alguna opción (una sola correcta).</li>
 * </ul>
 */
@Service
public class ValidationService {

    private final int requiredOptions;

    public ValidationService(@Value("${validation.required-options:4}") int requiredOptions) {
        this.requiredOptions = requiredOptions;
    }

    public ValidationResult validate(ValidationRequest request) {
        List<String> errors = new ArrayList<>();

        if (request == null) {
            return new ValidationResult(false, List.of("La solicitud es obligatoria"));
        }

        if (isBlank(request.text())) {
            errors.add("El enunciado no puede estar vacío");
        }

        List<OptionDTO> options = request.options();
        if (options == null || options.isEmpty()) {
            errors.add("Debe registrar exactamente " + requiredOptions + " opciones");
        } else {
            if (options.size() != requiredOptions) {
                errors.add("Debe registrar exactamente " + requiredOptions + " opciones (se recibieron " + options.size() + ")");
            }
            validateOptions(options, errors, request.correctOptionId());
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    private void validateOptions(List<OptionDTO> options, List<String> errors, String correctOptionId) {
        Set<String> ids = new HashSet<>();
        Set<String> texts = new HashSet<>();

        for (int i = 0; i < options.size(); i++) {
            OptionDTO option = options.get(i);
            if (option == null) {
                errors.add("La opción " + (i + 1) + " es nula");
                continue;
            }
            if (isBlank(option.id())) {
                errors.add("La opción " + (i + 1) + " no tiene id");
            } else if (!ids.add(option.id())) {
                errors.add("El id de opción '" + option.id() + "' está duplicado");
            }
            if (isBlank(option.text())) {
                errors.add("El texto de la opción " + (i + 1) + " no puede estar vacío");
            } else if (!texts.add(option.text().trim().toLowerCase())) {
                errors.add("El texto de la opción '" + option.text() + "' está repetido");
            }
        }

        if (isBlank(correctOptionId)) {
            errors.add("Debe indicar la opción correcta (correctOptionId)");
        } else if (!ids.contains(correctOptionId)) {
            errors.add("correctOptionId '" + correctOptionId + "' no coincide con ninguna opción");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
