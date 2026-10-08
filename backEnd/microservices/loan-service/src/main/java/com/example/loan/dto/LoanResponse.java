package com.example.loan.dto;

import com.example.loan.model.enums.LoanStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "A loan as returned by the API")
public record LoanResponse(
        Long id,
        Long bookId,
        Long memberId,
        LocalDate loanDate,
        LocalDate dueDate,
        LocalDate returnDate,
        LoanStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
