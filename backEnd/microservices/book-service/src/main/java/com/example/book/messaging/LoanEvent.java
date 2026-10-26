package com.example.book.messaging;

import java.time.Instant;

public record LoanEvent(
        String eventId,
        String eventType,
        Instant occurredAt,
        Long loanId,
        Long bookId,
        Long memberId
) {

    public static final String LOAN_CREATED = "LoanCreated";
    public static final String LOAN_RETURNED = "LoanReturned";
    public static final String LOAN_CANCELLED = "LoanCancelled";
}
