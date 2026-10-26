package com.example.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "A loan read from loan-service through OpenFeign")
public record LoanSummary(
        Long id,
        Long bookId,
        LocalDate loanDate,
        LocalDate dueDate,
        LocalDate returnDate,
        String status
) {

    public static final String RETURNED = "RETURNED";

    public boolean ongoing() {
        return !RETURNED.equals(status);
    }
}
