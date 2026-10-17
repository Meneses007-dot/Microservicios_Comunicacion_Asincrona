package co.edu.unicauca.lifecycleservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declaración idempotente de los elementos que este servicio usa.
 * Al declararlos todos los servicios, cada uno puede arrancar en cualquier orden.
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange saberproExchange() {
        return new DirectExchange(MessagingConstants.EXCHANGE, true, false);
    }

    @Bean
    public Queue submitReviewQueue() {
        return new Queue(MessagingConstants.SUBMIT_QUEUE, true);
    }

    @Bean
    public Binding submitReviewBinding(Queue submitReviewQueue, DirectExchange saberproExchange) {
        return BindingBuilder.bind(submitReviewQueue)
                .to(saberproExchange)
                .with(MessagingConstants.SUBMIT_ROUTING_KEY);
    }

    /** Convertidor JSON aplicado a listeners y a RabbitTemplate. */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
