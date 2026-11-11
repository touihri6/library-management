package com.example.member.dto;

import com.example.member.model.enums.MembershipType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "A member as returned by the API")
public record MemberResponse(
        @Schema(description = "Technical id of the member", example = "1")
        Long id,

        @Schema(description = "First name of the member", example = "Amine")
        String firstName,

        @Schema(description = "Last name of the member", example = "Ben Ali")
        String lastName,

        @Schema(description = "E-mail address, unique among members", example = "amine@mail.com")
        String email,

        @Schema(description = "Phone number", example = "22123456", nullable = true)
        String phone,

        @Schema(description = "Membership type", example = "STUDENT")
        MembershipType membershipType,

        @Schema(description = "Day the member joined the library", example = "2025-09-01")
        LocalDate joinDate,

        @Schema(description = "Keycloak id (sub) of the linked account; null when no account is linked yet", example = "5f0c2a9e-3b7d-4f1e-9a51-1c2d3e4f5a6b", nullable = true)
        String userId,

        @Schema(description = "When the member was created (UTC)", example = "2026-10-19T08:30:00Z")
        Instant createdAt,

        @Schema(description = "When the member was last modified (UTC)", example = "2026-10-20T14:05:00Z")
        Instant updatedAt,

        @Schema(description = "Keycloak id (sub) of the user who created the member", example = "5f0c2a9e-3b7d-4f1e-9a51-1c2d3e4f5a6b", nullable = true)
        String createdBy,

        @Schema(description = "Keycloak id (sub) of the user who last modified the member", example = "5f0c2a9e-3b7d-4f1e-9a51-1c2d3e4f5a6b", nullable = true)
        String updatedBy
) {
}
