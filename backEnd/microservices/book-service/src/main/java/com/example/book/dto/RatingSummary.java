package com.example.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Average rating of a book, read from review-service through OpenFeign")
public record RatingSummary(
        Long bookId,
        double average,
        long count
) {
}
