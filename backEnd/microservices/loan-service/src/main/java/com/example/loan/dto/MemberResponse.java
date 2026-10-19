package com.example.loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A member read from member-service through OpenFeign")
public record MemberResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String membershipType
) {
}
