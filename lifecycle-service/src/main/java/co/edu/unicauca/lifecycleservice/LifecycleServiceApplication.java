package co.edu.unicauca.lifecycleservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Lifecycle Service (:8084).
 *
 * <p><b>Única autoridad</b> sobre el estado de una pregunta. Recibe peticiones
 * de transición por REST y comandos por RabbitMQ, valida la máquina de estados
 * y guarda siempre un {@code OperationResult} para las operaciones asíncronas.</p>
 */
@SpringBootApplication
public class LifecycleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LifecycleServiceApplication.class, args);
    }
}
