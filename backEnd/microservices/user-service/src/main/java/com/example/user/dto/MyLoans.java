package com.example.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "The library member linked to the authenticated user, with their loans")
public record MyLoans(
        MemberSummary member,
        List<LoanSummary> loans
) {
}
