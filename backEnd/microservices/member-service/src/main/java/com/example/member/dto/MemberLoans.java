package com.example.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "A member with their loan history")
public record MemberLoans(
        MemberResponse member,
        List<LoanSummary> loans
) {
}
