package co.edu.unicauca.validationservice;

import co.edu.unicauca.validationservice.dto.OptionDTO;
import co.edu.unicauca.validationservice.dto.ValidationRequest;
import co.edu.unicauca.validationservice.dto.ValidationResult;
import co.edu.unicauca.validationservice.service.ValidationService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Pruebas de las reglas de negocio de la validación (sin contexto Spring). */
class ValidationServiceTest {

    private final ValidationService service = new ValidationService(4);

    private static List<OptionDTO> fourOptions() {
        return List.of(
                new OptionDTO("a", "3"),
                new OptionDTO("b", "4"),
                new OptionDTO("c", "5"),
                new OptionDTO("d", "6"));
    }

    @Test
    void preguntaValida() {
        ValidationResult result = service.validate(
                new ValidationRequest("¿Cuánto es 2+2?", fourOptions(), "b"));
        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void enunciadoVacioEsInvalido() {
        ValidationResult result = service.validate(
                new ValidationRequest("  ", fourOptions(), "b"));
        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(e -> e.contains("enunciado"));
    }

    @Test
    void cantidadIncorrectaDeOpcionesEsInvalida() {
        ValidationResult result = service.validate(
                new ValidationRequest("Pregunta", fourOptions().subList(0, 2), "a"));
        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(e -> e.contains("exactamente 4"));
    }

    @Test
    void idsDuplicadosYTextosRepetidosSonInvalidos() {
        List<OptionDTO> options = List.of(
                new OptionDTO("a", "3"),
                new OptionDTO("a", "3"),
                new OptionDTO("c", "5"),
                new OptionDTO("d", "6"));
        ValidationResult result = service.validate(new ValidationRequest("Pregunta", options, "a"));
        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(e -> e.contains("duplicado"));
        assertThat(result.errors()).anyMatch(e -> e.contains("repetido"));
    }

    @Test
    void claveQueNoCoincideEsInvalida() {
        ValidationResult result = service.validate(
                new ValidationRequest("Pregunta", fourOptions(), "z"));
        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(e -> e.contains("correctOptionId"));
    }
}
