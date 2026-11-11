package com.example.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Internal payload sent to member-service through OpenFeign to create the library member of a new MEMBER account")
public record MemberCreateRequest(
        @Schema(description = "First name of the member", example = "Amine")
        String firstName,

        @Schema(description = "Last name of the member", example = "Ben Ali")
        String lastName,

        @Schema(description = "E-mail of the member, same as the account", example = "amine@mail.com")
        String email,

        @Schema(description = "Phone number of the member, not known at registration", example = "+216 20 123 456", nullable = true)
        String phone,

        @Schema(description = "Membership type, always STANDARD at registration", example = "STANDARD")
        String membershipType,

        @Schema(description = "Day the member joined the library", example = "2026-10-01")
        LocalDate joinDate,

        @Schema(description = "Keycloak id (sub) of the account linked to the member", example = "5f0c2a9e-3b7d-4f1e-9a51-1c2d3e4f5a6b")
        String userId
) {

    public static final String STANDARD = "STANDARD";
}
