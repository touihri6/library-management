package com.example.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A library member read from member-service through OpenFeign")
public record MemberSummary(
        Long id,
        String firstName,
        String lastName,
        String email,
        String membershipType
) {
}
