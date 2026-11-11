package com.example.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A library member read from member-service through OpenFeign")
public record MemberSummary(
        @Schema(description = "Id of the member in member-service", example = "3")
        Long id,

        @Schema(description = "First name of the member", example = "Amine")
        String firstName,

        @Schema(description = "Last name of the member", example = "Ben Ali")
        String lastName,

        @Schema(description = "E-mail of the member", example = "amine@mail.com")
        String email,

        @Schema(description = "Membership type", example = "STANDARD")
        String membershipType
) {
}
