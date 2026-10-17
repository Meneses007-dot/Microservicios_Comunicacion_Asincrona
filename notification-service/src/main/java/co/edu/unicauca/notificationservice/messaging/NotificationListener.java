package co.edu.unicauca.notificationservice.messaging;

import co.edu.unicauca.notificationservice.config.MessagingConstants;
import co.edu.unicauca.notificationservice.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Consumidor puro del evento ReviewerAssigned. */
@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final EmailService emailService;

    public NotificationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = MessagingConstants.REVIEWER_ASSIGNED_QUEUE)
    public void onReviewerAssigned(ReviewerAssigned event) {
        log.info("Evento ReviewerAssigned recibido: {}", event);
        emailService.sendReviewerAssigned(event);
    }
}
