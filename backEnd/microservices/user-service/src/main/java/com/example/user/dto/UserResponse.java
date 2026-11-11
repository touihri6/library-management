package com.example.user.dto;

import com.example.user.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "A user as returned by the API")
public record UserResponse(
        @Schema(description = "Technical id of the user", example = "1")
        Long id,

        @Schema(description = "Keycloak id (sub) of the user", example = "5f0c2a9e-3b7d-4f1e-9a51-1c2d3e4f5a6b")
        String keycloakId,

        @Schema(description = "Login of the user", example = "amine")
        String username,

        @Schema(description = "E-mail address", example = "amine@mail.com")
        String email,

        @Schema(description = "First name", example = "Amine")
        String firstName,

        @Schema(description = "Last name", example = "Ben Ali")
        String lastName,

        @Schema(description = "Realm role of the user", example = "MEMBER")
        UserRole role,

        @Schema(description = "False when the account is disabled and can no longer log in", example = "true")
        boolean enabled,

        @Schema(description = "When the user was created (UTC)", example = "2026-10-01T09:15:00Z")
        Instant createdAt,

        @Schema(description = "When the user was last modified (UTC)", example = "2026-10-02T11:40:00Z")
        Instant updatedAt
) {
}
