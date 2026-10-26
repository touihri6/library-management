package com.example.loan.messaging;

import com.example.loan.model.entity.Loan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class LoanEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoanEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public LoanEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setConfirmCallback((correlation, ack, cause) -> {
            String id = correlation != null ? correlation.getId() : "?";
            if (ack) {
                log.debug("RabbitMQ confirmed event {}", id);
            } else {
                log.error("RabbitMQ did not confirm event {}: {}", id, cause);
            }
        });
        this.rabbitTemplate.setReturnsCallback(returned ->
                log.warn("Unroutable event (no queue bound for '{}'): {}",
                        returned.getRoutingKey(), returned.getReplyText()));
    }

    public void publishLoanCreated(Loan loan) {
        publishAfterCommit(RabbitConfig.LOAN_CREATED_KEY, LoanEvent.of(LoanEvent.LOAN_CREATED, loan));
    }

    public void publishLoanReturned(Loan loan) {
        publishAfterCommit(RabbitConfig.LOAN_RETURNED_KEY, LoanEvent.of(LoanEvent.LOAN_RETURNED, loan));
    }

    public void publishLoanCancelled(Loan loan) {
        publishAfterCommit(RabbitConfig.LOAN_CANCELLED_KEY, LoanEvent.of(LoanEvent.LOAN_CANCELLED, loan));
    }

    private void publishAfterCommit(String routingKey, LoanEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(routingKey, event);
                }
            });
        } else {
            send(routingKey, event);
        }
    }

    private void send(String routingKey, LoanEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, event,
                    message -> {
                        message.getMessageProperties().setMessageId(event.eventId());
                        message.getMessageProperties().setType(event.eventType());
                        return message;
                    },
                    new CorrelationData(event.eventId()));
            log.info("Event {} published for loan {} (eventId={})", event.eventType(), event.loanId(), event.eventId());
        } catch (AmqpException ex) {
            log.error("Failed to publish event {} for loan {}: {}", event.eventType(), event.loanId(), ex.getMessage());
        }
    }
}
