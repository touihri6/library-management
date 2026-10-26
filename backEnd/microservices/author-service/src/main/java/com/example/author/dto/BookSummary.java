package com.example.author.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A book read from book-service through OpenFeign")
public record BookSummary(
        Long id,
        String title,
        String author,
        String isbn,
        String genre,
        Integer publicationYear,
        int availableCopies,
        boolean available
) {
}
