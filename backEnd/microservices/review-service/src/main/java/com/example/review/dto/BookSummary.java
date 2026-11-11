package com.example.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A book read from book-service through OpenFeign")
public record BookSummary(
        @Schema(description = "Technical id of the book in book-service", example = "1")
        Long id,

        @Schema(description = "Title of the book", example = "Dune")
        String title
) {
}
