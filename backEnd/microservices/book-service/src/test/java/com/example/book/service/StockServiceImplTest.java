package com.example.book.service;

import com.example.book.messaging.LoanEvent;
import com.example.book.model.entity.ProcessedEvent;
import com.example.book.repository.BookRepository;
import com.example.book.repository.ProcessedEventRepository;
import com.example.book.service.impl.StockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    private StockService stockService;

    @BeforeEach
    void setUp() {
        stockService = new StockServiceImpl(bookRepository, processedEventRepository);
    }

    @Test
    void apply_loanCreated_decrementsAndRecordsEvent() {
        when(bookRepository.decrementAvailableCopies(eq(7L), any(Instant.class))).thenReturn(1);

        stockService.apply(event("e-1", LoanEvent.LOAN_CREATED));

        verify(bookRepository).decrementAvailableCopies(eq(7L), any(Instant.class));
        ArgumentCaptor<ProcessedEvent> saved = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(processedEventRepository).save(saved.capture());
        assertThat(saved.getValue().getEventId()).isEqualTo("e-1");
        assertThat(saved.getValue().getEventType()).isEqualTo(LoanEvent.LOAN_CREATED);
    }

    @Test
    void apply_loanReturned_increments() {
        when(bookRepository.incrementAvailableCopies(eq(7L), any(Instant.class))).thenReturn(1);

        stockService.apply(event("e-2", LoanEvent.LOAN_RETURNED));

        verify(bookRepository).incrementAvailableCopies(eq(7L), any(Instant.class));
        verify(bookRepository, never()).decrementAvailableCopies(any(), any());
    }

    @Test
    void apply_loanCancelled_increments() {
        when(bookRepository.incrementAvailableCopies(eq(7L), any(Instant.class))).thenReturn(1);

        stockService.apply(event("e-3", LoanEvent.LOAN_CANCELLED));

        verify(bookRepository).incrementAvailableCopies(eq(7L), any(Instant.class));
    }

    @Test
    void apply_alreadyProcessed_isIgnored() {
        when(processedEventRepository.existsById("e-1")).thenReturn(true);

        stockService.apply(event("e-1", LoanEvent.LOAN_CREATED));

        verifyNoInteractions(bookRepository);
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    void apply_outOfStock_stillRecordsEvent() {
        when(bookRepository.decrementAvailableCopies(eq(7L), any(Instant.class))).thenReturn(0);

        stockService.apply(event("e-4", LoanEvent.LOAN_CREATED));

        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    @Test
    void apply_unknownType_doesNotTouchStock() {
        stockService.apply(event("e-5", "LoanExtended"));

        verifyNoInteractions(bookRepository);
        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    private static LoanEvent event(String eventId, String eventType) {
        return new LoanEvent(eventId, eventType, Instant.now(), 42L, 7L, 3L);
    }
}
