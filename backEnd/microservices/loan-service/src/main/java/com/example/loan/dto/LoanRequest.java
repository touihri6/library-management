package com.example.loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

@Schema(description = "Payload used to create or update a loan")
public record LoanRequest(

        @Schema(example = "1")
        @NotNull @Positive
        Long bookId,

        @Schema(example = "1")
        @NotNull @Positive
        Long memberId,

        @Schema(example = "2026-10-01")
        @NotNull
        LocalDate loanDate,

        @Schema(example = "2026-10-15")
        @NotNull
        LocalDate dueDate
) {
}
