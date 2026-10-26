package com.example.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A book read from book-service through OpenFeign")
public record BookSummary(
        Long id,
        String title
) {
}
