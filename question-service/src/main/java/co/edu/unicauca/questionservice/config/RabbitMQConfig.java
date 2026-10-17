package co.edu.unicauca.questionservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Declaración idempotente del exchange, la cola de comandos y su binding. */
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

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
