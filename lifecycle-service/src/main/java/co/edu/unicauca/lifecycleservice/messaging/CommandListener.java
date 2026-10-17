package co.edu.unicauca.lifecycleservice.messaging;

import co.edu.unicauca.lifecycleservice.config.MessagingConstants;
import co.edu.unicauca.lifecycleservice.service.LifecycleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor del comando SubmitQuestionForReview.
 * El tipo del mensaje se infiere del parámetro del método (SubmitCommandDTO).
 */
@Component
public class CommandListener {

    private static final Logger log = LoggerFactory.getLogger(CommandListener.class);

    private final LifecycleService lifecycleService;

    public CommandListener(LifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    @RabbitListener(queues = MessagingConstants.SUBMIT_QUEUE)
    public void onSubmitForReview(SubmitCommandDTO command) {
        log.info("Comando recibido: {}", command);
        lifecycleService.processSubmitCommand(command);
    }
}
