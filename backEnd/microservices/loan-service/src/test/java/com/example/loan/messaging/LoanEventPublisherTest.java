package com.example.loan.messaging;

import com.example.loan.model.entity.Loan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.net.ConnectException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoanEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private LoanEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new LoanEventPublisher(rabbitTemplate);
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void publishLoanCreated_withoutTransaction_sendsImmediately() {
        publisher.publishLoanCreated(loan());

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq(RabbitConfig.EXCHANGE), eq(RabbitConfig.LOAN_CREATED_KEY),
                payload.capture(), any(MessagePostProcessor.class), any(CorrelationData.class));
        LoanEvent event = (LoanEvent) payload.getValue();
        assertThat(event.eventType()).isEqualTo(LoanEvent.LOAN_CREATED);
        assertThat(event.eventId()).isNotBlank();
        assertThat(event.loanId()).isEqualTo(42L);
        assertThat(event.bookId()).isEqualTo(7L);
        assertThat(event.memberId()).isEqualTo(3L);
    }

    @Test
    void publishLoanReturned_insideTransaction_waitsForCommit() {
        TransactionSynchronizationManager.initSynchronization();

        publisher.publishLoanReturned(loan());

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class),
                any(MessagePostProcessor.class), any(CorrelationData.class));

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);

        verify(rabbitTemplate).convertAndSend(eq(RabbitConfig.EXCHANGE), eq(RabbitConfig.LOAN_RETURNED_KEY),
                any(LoanEvent.class), any(MessagePostProcessor.class), any(CorrelationData.class));
    }

    @Test
    void publishLoanCancelled_usesCancelledRoutingKey() {
        publisher.publishLoanCancelled(loan());

        verify(rabbitTemplate).convertAndSend(eq(RabbitConfig.EXCHANGE), eq(RabbitConfig.LOAN_CANCELLED_KEY),
                any(LoanEvent.class), any(MessagePostProcessor.class), any(CorrelationData.class));
    }

    @Test
    void publish_whenBrokerDown_doesNotThrow() {
        doThrow(new AmqpConnectException(new ConnectException("refused")))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class),
                        any(MessagePostProcessor.class), any(CorrelationData.class));

        assertThatCode(() -> publisher.publishLoanCreated(loan())).doesNotThrowAnyException();
    }

    private static Loan loan() {
        Loan loan = new Loan();
        loan.setId(42L);
        loan.setBookId(7L);
        loan.setMemberId(3L);
        return loan;
    }
}
