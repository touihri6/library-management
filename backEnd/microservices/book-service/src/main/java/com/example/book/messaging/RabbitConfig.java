package com.example.book.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "library.events";
    public static final String LOAN_ROUTING_PATTERN = "loan.*";
    public static final String LOAN_EVENTS_QUEUE = "book.loan-events";
    public static final String DEAD_LETTER_EXCHANGE = "library.events.dlx";
    public static final String LOAN_EVENTS_DLQ = "book.loan-events.dlq";

    @Bean
    public TopicExchange libraryEventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue loanEventsQueue() {
        return QueueBuilder.durable(LOAN_EVENTS_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(LOAN_EVENTS_DLQ)
                .build();
    }

    @Bean
    public Queue loanEventsDeadLetterQueue() {
        return QueueBuilder.durable(LOAN_EVENTS_DLQ).build();
    }

    @Bean
    public Binding loanEventsBinding() {
        return BindingBuilder.bind(loanEventsQueue()).to(libraryEventsExchange()).with(LOAN_ROUTING_PATTERN);
    }

    @Bean
    public Binding loanEventsDeadLetterBinding() {
        return BindingBuilder.bind(loanEventsDeadLetterQueue()).to(deadLetterExchange()).with(LOAN_EVENTS_DLQ);
    }

    @Bean
    public MessageConverter jsonMessageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonMessageConverter(jsonMapper);
    }
}
