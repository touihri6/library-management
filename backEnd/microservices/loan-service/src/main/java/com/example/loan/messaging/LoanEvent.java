package com.example.loan.messaging;

import com.example.loan.model.entity.Loan;

import java.time.Instant;
import java.util.UUID;

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

    public static LoanEvent of(String eventType, Loan loan) {
        return new LoanEvent(UUID.randomUUID().toString(), eventType, Instant.now(),
                loan.getId(), loan.getBookId(), loan.getMemberId());
    }
}
