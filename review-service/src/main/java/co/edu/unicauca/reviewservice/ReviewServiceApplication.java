package co.edu.unicauca.reviewservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Review Service (:8085).
 *
 * <p>Gestiona asignaciones de revisores y resultados. Solicita transiciones a
 * Lifecycle (que es el dueño del estado) y, solo cuando la asignación queda
 * confirmada, publica el evento {@code ReviewerAssigned} para Notification Service.</p>
 */
@SpringBootApplication
public class ReviewServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReviewServiceApplication.class, args);
    }
}
