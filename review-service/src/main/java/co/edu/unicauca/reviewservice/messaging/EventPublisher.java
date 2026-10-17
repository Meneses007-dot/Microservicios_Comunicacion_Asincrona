package co.edu.unicauca.reviewservice.messaging;

import co.edu.unicauca.reviewservice.config.MessagingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** Publica el evento ReviewerAssigned en el exchange directo. */
@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishReviewerAssigned(ReviewerAssigned event) {
        rabbitTemplate.convertAndSend(
                MessagingConstants.EXCHANGE,
                MessagingConstants.REVIEWER_ASSIGNED_ROUTING_KEY,
                event);
        log.info("Evento publicado en {} con routing key {}: {}",
                MessagingConstants.EXCHANGE, MessagingConstants.REVIEWER_ASSIGNED_ROUTING_KEY, event);
    }
}
