package co.edu.unicauca.notificationservice.service;

import co.edu.unicauca.notificationservice.messaging.ReviewerAssigned;
import co.edu.unicauca.notificationservice.model.NotificationRecord;
import co.edu.unicauca.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Simula el envío de correo. El retardo evidencia que la notificación corre
 * en el consumidor y no bloquea la asignación hecha por Review Service.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String CHANNEL = "EMAIL";

    private final NotificationRepository notificationRepository;
    private final long simulatedDelayMs;

    public EmailService(NotificationRepository notificationRepository,
                        @Value("${notification.simulated-delay-ms:1500}") long simulatedDelayMs) {
        this.notificationRepository = notificationRepository;
        this.simulatedDelayMs = simulatedDelayMs;
    }

    public NotificationRecord sendReviewerAssigned(ReviewerAssigned event) {
        String message = String.format(
                "Notificación: el revisor %s fue asignado a la pregunta %s (asignado por %s)",
                event.reviewerId(), event.questionId(), event.assignedBy());

        log.info("[SIMULACIÓN EMAIL] Enviando... {}", message);
        sleep(simulatedDelayMs);
        log.info("[SIMULACIÓN EMAIL] Enviado: {}", message);

        return notificationRepository.save(
                new NotificationRecord(event.questionId(), event.reviewerId(), event.assignedBy(),
                        CHANNEL, message, Instant.now()));
    }

    public List<NotificationRecord> findAll() {
        return notificationRepository.findAll();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
