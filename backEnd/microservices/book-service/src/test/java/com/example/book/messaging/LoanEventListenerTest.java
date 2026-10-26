package com.example.book.messaging;

import com.example.book.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class LoanEventListenerTest {

    @Mock
    private StockService stockService;

    private LoanEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new LoanEventListener(stockService);
    }

    @Test
    void onLoanEvent_validEvent_delegatesToStockService() {
        LoanEvent event = new LoanEvent("e-1", LoanEvent.LOAN_CREATED, Instant.now(), 42L, 7L, 3L);

        listener.onLoanEvent(event);

        verify(stockService).apply(event);
    }

    @Test
    void onLoanEvent_withoutBookId_isRejected() {
        LoanEvent event = new LoanEvent("e-1", LoanEvent.LOAN_CREATED, Instant.now(), 42L, null, 3L);

        assertThatThrownBy(() -> listener.onLoanEvent(event))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
        verifyNoInteractions(stockService);
    }

    @Test
    void onLoanEvent_withoutEventId_isRejected() {
        LoanEvent event = new LoanEvent(null, LoanEvent.LOAN_CREATED, Instant.now(), 42L, 7L, 3L);

        assertThatThrownBy(() -> listener.onLoanEvent(event))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
        verifyNoInteractions(stockService);
    }
}
