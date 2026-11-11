package com.example.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "A loan read from loan-service through OpenFeign")
public record LoanSummary(
        @Schema(description = "Technical id of the loan", example = "1")
        Long id,

        @Schema(description = "Id of the borrowed book in book-service", example = "3")
        Long bookId,

        @Schema(description = "Day the book was borrowed", example = "2026-10-01")
        LocalDate loanDate,

        @Schema(description = "Day the book must be returned", example = "2026-10-15")
        LocalDate dueDate,

        @Schema(description = "Day the book was returned; null while the loan is open", example = "2026-10-12", nullable = true)
        LocalDate returnDate,

        @Schema(description = "Status of the loan: ONGOING, RETURNED or LATE", example = "ONGOING")
        String status
) {

    public static final String RETURNED = "RETURNED";

    public boolean ongoing() {
        return !RETURNED.equals(status);
    }
}
