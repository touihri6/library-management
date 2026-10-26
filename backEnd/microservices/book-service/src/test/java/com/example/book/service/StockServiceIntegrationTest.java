package com.example.book.service;

import com.example.book.messaging.LoanEvent;
import com.example.book.repository.BookRepository;
import com.example.book.repository.ProcessedEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class StockServiceIntegrationTest {

    private static final long EFFECTIVE_JAVA_ID = 6L;

    @Autowired
    private StockService stockService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Test
    void loanCreated_decrementsAvailableCopies() {
        stockService.apply(event("e-1", LoanEvent.LOAN_CREATED, EFFECTIVE_JAVA_ID));

        assertThat(copiesOf(EFFECTIVE_JAVA_ID)).isZero();
        assertThat(processedEventRepository.existsById("e-1")).isTrue();
    }

    @Test
    void loanCreated_onLastCopyTwice_neverGoesNegative() {
        stockService.apply(event("e-1", LoanEvent.LOAN_CREATED, EFFECTIVE_JAVA_ID));
        stockService.apply(event("e-2", LoanEvent.LOAN_CREATED, EFFECTIVE_JAVA_ID));

        assertThat(copiesOf(EFFECTIVE_JAVA_ID)).isZero();
    }

    @Test
    void duplicateEvent_isAppliedOnce() {
        LoanEvent event = event("e-1", LoanEvent.LOAN_RETURNED, EFFECTIVE_JAVA_ID);

        stockService.apply(event);
        stockService.apply(event);

        assertThat(copiesOf(EFFECTIVE_JAVA_ID)).isEqualTo(2);
    }

    @Test
    void loanCreatedThenReturned_restoresStock() {
        stockService.apply(event("e-1", LoanEvent.LOAN_CREATED, EFFECTIVE_JAVA_ID));
        stockService.apply(event("e-2", LoanEvent.LOAN_RETURNED, EFFECTIVE_JAVA_ID));

        assertThat(copiesOf(EFFECTIVE_JAVA_ID)).isEqualTo(1);
    }

    @Test
    void loanCancelled_incrementsAvailableCopies() {
        stockService.apply(event("e-1", LoanEvent.LOAN_CANCELLED, EFFECTIVE_JAVA_ID));

        assertThat(copiesOf(EFFECTIVE_JAVA_ID)).isEqualTo(2);
    }

    @Test
    void stockChange_bumpsVersion() {
        long before = bookRepository.findById(EFFECTIVE_JAVA_ID).orElseThrow().getVersion();

        stockService.apply(event("e-1", LoanEvent.LOAN_RETURNED, EFFECTIVE_JAVA_ID));

        assertThat(bookRepository.findById(EFFECTIVE_JAVA_ID).orElseThrow().getVersion()).isEqualTo(before + 1);
    }

    @Test
    void unknownBook_isIgnored() {
        stockService.apply(event("e-1", LoanEvent.LOAN_CREATED, 999L));

        assertThat(processedEventRepository.existsById("e-1")).isTrue();
    }

    private int copiesOf(long bookId) {
        return bookRepository.findById(bookId).orElseThrow().getAvailableCopies();
    }

    private static LoanEvent event(String eventId, String eventType, long bookId) {
        return new LoanEvent(eventId, eventType, Instant.now(), 42L, bookId, 3L);
    }
}
