package com.example.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "The library member linked to the authenticated user, with their loans")
public record MyLoans(
        @Schema(description = "Library member linked to the account, found by Keycloak id then by e-mail")
        MemberSummary member,

        @Schema(description = "Loans of the member, read from loan-service")
        List<LoanSummary> loans
) {
}
