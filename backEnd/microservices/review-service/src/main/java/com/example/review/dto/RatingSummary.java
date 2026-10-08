package com.example.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Average rating of a book")
public record RatingSummary(
        Long bookId,
        double average,
        long count
) {
}
