package com.example.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "A review as returned by the API")
public record ReviewResponse(
        Long id,
        Long bookId,
        String reviewerName,
        Integer rating,
        String comment,
        Instant createdAt,
        Instant updatedAt
) {
}
