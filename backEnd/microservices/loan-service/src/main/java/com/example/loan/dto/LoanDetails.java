package com.example.loan.dto;

import com.example.loan.model.enums.LoanStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "A loan with its book and member, read from book-service and member-service")
public record LoanDetails(
        @Schema(description = "Technical id of the loan", example = "1")
        Long id,

        @Schema(description = "Day the book was borrowed", example = "2026-10-01")
        LocalDate loanDate,

        @Schema(description = "Day the book must be returned", example = "2026-10-15")
        LocalDate dueDate,

        @Schema(description = "Day the book was returned; null while the loan is open", example = "2026-10-12", nullable = true)
        LocalDate returnDate,

        @Schema(description = "Status of the loan", example = "ONGOING")
        LoanStatus status,

        @Schema(description = "The borrowed book, read from book-service")
        BookResponse book,

        @Schema(description = "The borrowing member, read from member-service")
        MemberResponse member
) {
}
