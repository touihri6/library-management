package com.example.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A member read from member-service through OpenFeign")
public record MemberContact(
        @Schema(description = "Technical id of the member in member-service", example = "1")
        Long id,

        @Schema(description = "First name of the member", example = "Nour")
        String firstName,

        @Schema(description = "Last name of the member", example = "Haddad")
        String lastName,

        @Schema(description = "Email address the announcement is sent to", example = "nour@mail.com")
        String email
) {
}
