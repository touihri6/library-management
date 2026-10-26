package com.example.user.dto;

import com.example.user.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "A user as returned by the API")
public record UserResponse(
        Long id,
        String keycloakId,
        String username,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
