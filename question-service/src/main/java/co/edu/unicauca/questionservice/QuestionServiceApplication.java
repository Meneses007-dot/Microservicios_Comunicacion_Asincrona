package co.edu.unicauca.questionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Question Service (:8083).
 *
 * <p>Dueño del contenido de la pregunta. Coordina el flujo: valida de forma
 * síncrona contra Validation Service, persiste la pregunta en DRAFT y publica
 * el comando asíncrono de envío a revisión. <b>No</b> es dueño del estado:
 * solo guarda un campo informativo que nunca modifica.</p>
 */
@SpringBootApplication
public class QuestionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuestionServiceApplication.class, args);
    }
}
