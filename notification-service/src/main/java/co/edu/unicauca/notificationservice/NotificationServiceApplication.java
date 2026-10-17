package co.edu.unicauca.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Notification Service (:8086).
 *
 * <p><b>Solo consume</b> eventos de RabbitMQ (comunicación asíncrona pura).
 * No expone ninguna operación que bloquee a los demás servicios; el envío se
 * simula con un retardo y un log, y queda un registro consultable.</p>
 */
@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
