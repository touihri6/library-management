package com.example.book.service.impl;

import com.example.book.messaging.LoanEvent;
import com.example.book.model.entity.ProcessedEvent;
import com.example.book.repository.BookRepository;
import com.example.book.repository.ProcessedEventRepository;
import com.example.book.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class StockServiceImpl implements StockService {

    private static final Logger log = LoggerFactory.getLogger(StockServiceImpl.class);

    private final BookRepository bookRepository;
    private final ProcessedEventRepository processedEventRepository;

    public StockServiceImpl(BookRepository bookRepository, ProcessedEventRepository processedEventRepository) {
        this.bookRepository = bookRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @Override
    @Transactional
    public void apply(LoanEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            log.info("Event {} already processed, ignored", event.eventId());
            return;
        }
        switch (event.eventType()) {
            case LoanEvent.LOAN_CREATED -> decrement(event);
            case LoanEvent.LOAN_RETURNED, LoanEvent.LOAN_CANCELLED -> increment(event);
            default -> log.warn("Unknown event type {} (eventId={}), ignored", event.eventType(), event.eventId());
        }
        processedEventRepository.save(new ProcessedEvent(event.eventId(), event.eventType(), Instant.now()));
    }

    private void decrement(LoanEvent event) {
        if (bookRepository.decrementAvailableCopies(event.bookId(), Instant.now()) == 0) {
            log.warn("Book {} not found or out of stock, loan {} not deducted", event.bookId(), event.loanId());
        }
    }

    private void increment(LoanEvent event) {
        if (bookRepository.incrementAvailableCopies(event.bookId(), Instant.now()) == 0) {
            log.warn("Book {} not found, loan {} not restocked", event.bookId(), event.loanId());
        }
    }
}
