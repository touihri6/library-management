package com.example.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A book with its rating; rating is null when review-service is unavailable")
public record BookDetails(
        BookResponse book,
        RatingSummary rating
) {
}
