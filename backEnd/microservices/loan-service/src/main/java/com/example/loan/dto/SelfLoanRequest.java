package com.example.loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Payload used by a signed-in member to borrow a book for themselves")
public record SelfLoanRequest(

        @Schema(description = "Id of the book to borrow in book-service", example = "3")
        @NotNull @Positive
        Long bookId
) {
}
