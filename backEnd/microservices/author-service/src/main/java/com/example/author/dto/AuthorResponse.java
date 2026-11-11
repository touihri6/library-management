package com.example.author.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "An author as returned by the API")
public record AuthorResponse(
        @Schema(description = "Technical id of the author", example = "1")
        Long id,

        @Schema(description = "First name of the author", example = "Frank")
        String firstName,

        @Schema(description = "Last name of the author", example = "Herbert")
        String lastName,

        @Schema(description = "Nationality of the author", example = "American")
        String nationality,

        @Schema(description = "Year of birth", example = "1920", nullable = true)
        Integer birthYear,

        @Schema(description = "Short biography", example = "Author of the Dune saga", nullable = true)
        String biography,

        @Schema(description = "When the author was created (UTC)", example = "2026-10-19T08:30:00Z")
        Instant createdAt,

        @Schema(description = "When the author was last modified (UTC)", example = "2026-10-20T14:05:00Z")
        Instant updatedAt,

        @Schema(description = "Keycloak id (sub) of the user who created the author", example = "5f0c2a9e-3b7d-4f1e-9a51-1c2d3e4f5a6b", nullable = true)
        String createdBy,

        @Schema(description = "Keycloak id (sub) of the user who last modified the author", example = "5f0c2a9e-3b7d-4f1e-9a51-1c2d3e4f5a6b", nullable = true)
        String updatedBy
) {
}
