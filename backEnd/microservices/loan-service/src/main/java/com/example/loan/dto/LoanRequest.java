package com.example.loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

@Schema(description = "Payload used to create or update a loan")
public record LoanRequest(

        @Schema(description = "Id of the book in book-service; cannot be changed on update", example = "3")
        @NotNull @Positive
        Long bookId,

        @Schema(description = "Id of the member in member-service", example = "1")
        @NotNull @Positive
        Long memberId,

        @Schema(description = "Day the book is borrowed", example = "2026-10-01")
        @NotNull
        LocalDate loanDate,

        @Schema(description = "Day the book must be returned; must be after loanDate", example = "2026-10-15")
        @NotNull
        LocalDate dueDate
) {
}
