package com.example.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A book with its rating; rating is null when review-service is unavailable")
public record BookDetails(
        @Schema(description = "The book")
        BookResponse book,

        @Schema(description = "Average rating from review-service; null when review-service is unavailable", nullable = true)
        RatingSummary rating
) {
}
