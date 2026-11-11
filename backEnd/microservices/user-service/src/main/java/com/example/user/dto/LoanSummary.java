package com.example.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "A loan read from loan-service through OpenFeign")
public record LoanSummary(
        @Schema(description = "Id of the loan in loan-service", example = "12")
        Long id,

        @Schema(description = "Id of the borrowed book in book-service", example = "1")
        Long bookId,

        @Schema(description = "Day the loan started", example = "2026-10-01")
        LocalDate loanDate,

        @Schema(description = "Day the book must be returned", example = "2026-10-15")
        LocalDate dueDate,

        @Schema(description = "Day the book was returned, null while the loan is in progress", example = "2026-10-12", nullable = true)
        LocalDate returnDate,

        @Schema(description = "Status of the loan in loan-service", example = "ACTIVE")
        String status
) {
}
