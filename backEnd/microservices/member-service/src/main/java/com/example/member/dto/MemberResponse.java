package com.example.member.dto;

import com.example.member.model.enums.MembershipType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "A member as returned by the API")
public record MemberResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        MembershipType membershipType,
        LocalDate joinDate,
        Instant createdAt,
        Instant updatedAt
) {
}
