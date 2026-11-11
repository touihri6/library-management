package com.example.loan.dto;

import com.example.loan.model.enums.LoanStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "A loan as returned by the API")
public record LoanResponse(
        @Schema(description = "Technical id of the loan", example = "1")
        Long id,

        @Schema(description = "Id of the borrowed book in book-service", example = "3")
        Long bookId,

        @Schema(description = "Id of the borrowing member in member-service", example = "1")
        Long memberId,

        @Schema(description = "Day the book was borrowed", example = "2026-10-01")
        LocalDate loanDate,

        @Schema(description = "Day the book must be returned", example = "2026-10-15")
        LocalDate dueDate,

        @Schema(description = "Day the book was returned; null while the loan is open", example = "2026-10-12", nullable = true)
        LocalDate returnDate,

        @Schema(description = "Status of the loan", example = "ONGOING")
        LoanStatus status,

        @Schema(description = "When the loan was created (UTC)", example = "2026-10-01T09:15:00Z")
        Instant createdAt,

        @Schema(description = "When the loan was last modified (UTC)", example = "2026-10-12T16:40:00Z")
        Instant updatedAt,

        @Schema(description = "Keycloak id (sub) of the user who created the loan", example = "5f0c2a9e-3b7d-4f1e-9a51-1c2d3e4f5a6b", nullable = true)
        String createdBy,

        @Schema(description = "Keycloak id (sub) of the user who last modified the loan", example = "5f0c2a9e-3b7d-4f1e-9a51-1c2d3e4f5a6b", nullable = true)
        String updatedBy
) {
}
