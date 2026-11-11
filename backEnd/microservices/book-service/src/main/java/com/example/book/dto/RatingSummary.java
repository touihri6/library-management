package com.example.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Average rating of a book, read from review-service through OpenFeign")
public record RatingSummary(
        @Schema(description = "Id of the rated book", example = "1")
        Long bookId,

        @Schema(description = "Average rating from 1 to 5, rounded to one decimal; 0.0 when there is no review", example = "4.5")
        double average,

        @Schema(description = "Number of reviews", example = "2")
        long count
) {
}
