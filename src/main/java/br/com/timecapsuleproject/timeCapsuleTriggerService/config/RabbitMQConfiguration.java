package br.com.timecapsuleproject.timeCapsuleTriggerService.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static br.com.timecapsuleproject.timeCapsuleService.constants.RabbitMqConstants.*;

@Configuration
public class RabbitMQConfiguration {

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue capsuleOpenedQueue() {
        return new Queue(QUEUE_CAPSULE_OPENED, true);
    }

    @Bean
    public Binding bindingCapsuleCreationTrigger(Queue capsuleOpenedQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(capsuleOpenedQueue).to(notificationExchange).with(ROUTING_KEY_CAPSULE_OPENED);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}