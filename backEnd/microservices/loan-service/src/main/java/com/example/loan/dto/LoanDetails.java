package com.example.loan.dto;

import com.example.loan.model.enums.LoanStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "A loan with its book and member, read from book-service and member-service")
public record LoanDetails(
        Long id,
        LocalDate loanDate,
        LocalDate dueDate,
        LocalDate returnDate,
        LoanStatus status,
        BookResponse book,
        MemberResponse member
) {
}
