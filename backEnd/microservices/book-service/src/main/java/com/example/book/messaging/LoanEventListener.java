package com.example.book.messaging;

import com.example.book.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class LoanEventListener {

    private static final Logger log = LoggerFactory.getLogger(LoanEventListener.class);

    private final StockService stockService;

    public LoanEventListener(StockService stockService) {
        this.stockService = stockService;
    }

    @RabbitListener(queues = RabbitConfig.LOAN_EVENTS_QUEUE)
    public void onLoanEvent(LoanEvent event) {
        if (event.eventId() == null || event.eventType() == null || event.bookId() == null) {
            throw new AmqpRejectAndDontRequeueException("Invalid loan event: " + event);
        }
        log.info("Event {} received for book {} (eventId={})", event.eventType(), event.bookId(), event.eventId());
        stockService.apply(event);
    }
}
