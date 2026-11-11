package com.example.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "A review as returned by the API")
public record ReviewResponse(
        @Schema(description = "Technical id of the review", example = "1")
        Long id,

        @Schema(description = "Id of the reviewed book", example = "1")
        Long bookId,

        @Schema(description = "Keycloak id (sub) of the review's author", example = "5f0c2a9e-3b7d-4f1e-9a51-1c2d3e4f5a6b", nullable = true)
        String userId,

        @Schema(description = "Name displayed for the reviewer", example = "Nour Haddad")
        String reviewerName,

        @Schema(description = "Rating from 1 to 5", example = "5")
        Integer rating,

        @Schema(description = "Free comment", example = "A masterpiece of science fiction", nullable = true)
        String comment,

        @Schema(description = "When the review was created (UTC)", example = "2026-10-19T08:30:00Z")
        Instant createdAt,

        @Schema(description = "When the review was last modified (UTC)", example = "2026-10-20T14:05:00Z")
        Instant updatedAt,

        @Schema(description = "Keycloak id (sub) of the user who created the review", example = "5f0c2a9e-3b7d-4f1e-9a51-1c2d3e4f5a6b", nullable = true)
        String createdBy,

        @Schema(description = "Keycloak id (sub) of the user who last modified the review", example = "5f0c2a9e-3b7d-4f1e-9a51-1c2d3e4f5a6b", nullable = true)
        String updatedBy
) {
}
