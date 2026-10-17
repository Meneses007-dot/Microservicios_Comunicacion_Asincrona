package co.edu.unicauca.reviewservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Declaración idempotente del exchange, la cola de eventos y su binding. */
@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange saberproExchange() {
        return new DirectExchange(MessagingConstants.EXCHANGE, true, false);
    }

    @Bean
    public Queue reviewerAssignedQueue() {
        return new Queue(MessagingConstants.REVIEWER_ASSIGNED_QUEUE, true);
    }

    @Bean
    public Binding reviewerAssignedBinding(Queue reviewerAssignedQueue, DirectExchange saberproExchange) {
        return BindingBuilder.bind(reviewerAssignedQueue)
                .to(saberproExchange)
                .with(MessagingConstants.REVIEWER_ASSIGNED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
