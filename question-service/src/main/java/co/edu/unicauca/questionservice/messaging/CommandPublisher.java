package co.edu.unicauca.questionservice.messaging;

import co.edu.unicauca.questionservice.config.MessagingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** Publica el comando de envío a revisión en el exchange directo. */
@Component
public class CommandPublisher {

    private static final Logger log = LoggerFactory.getLogger(CommandPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public CommandPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishSubmit(SubmitCommandDTO command) {
        rabbitTemplate.convertAndSend(
                MessagingConstants.EXCHANGE,
                MessagingConstants.SUBMIT_ROUTING_KEY,
                command);
        log.info("Comando publicado en {} con routing key {}: {}",
                MessagingConstants.EXCHANGE, MessagingConstants.SUBMIT_ROUTING_KEY, command);
    }
}
