package com.example.loan.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "library.events";
    public static final String LOAN_CREATED_KEY = "loan.created";
    public static final String LOAN_RETURNED_KEY = "loan.returned";
    public static final String LOAN_CANCELLED_KEY = "loan.cancelled";

    @Bean
    public TopicExchange libraryEventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonMessageConverter(jsonMapper);
    }
}
