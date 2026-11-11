package com.example.loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A member read from member-service through OpenFeign")
public record MemberResponse(
        @Schema(description = "Technical id of the member", example = "1")
        Long id,

        @Schema(description = "First name of the member", example = "Amine")
        String firstName,

        @Schema(description = "Last name of the member", example = "Ben Ali")
        String lastName,

        @Schema(description = "E-mail address of the member", example = "amine@mail.com")
        String email,

        @Schema(description = "Membership type: STUDENT, STANDARD or PREMIUM", example = "STUDENT")
        String membershipType
) {
}
