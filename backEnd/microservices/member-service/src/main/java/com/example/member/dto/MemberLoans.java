package com.example.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "A member with their loan history")
public record MemberLoans(
        @Schema(description = "The member")
        MemberResponse member,

        @Schema(description = "Loans of the member read from loan-service, most recent first")
        List<LoanSummary> loans
) {
}
