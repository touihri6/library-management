package com.example.loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Payload used by a signed-in member to borrow a book for themselves")
public record SelfLoanRequest(

        @Schema(example = "1")
        @NotNull @Positive
        Long bookId
) {
}
