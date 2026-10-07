package com.example.book.dto;

import com.example.book.model.enums.Genre;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "A book as returned by the API")
public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        Genre genre,
        Integer publicationYear,
        BigDecimal price,
        int availableCopies,
        @Schema(description = "Derived field: true when at least one copy is available")
        boolean available,
        Instant createdAt,
        Instant updatedAt
) {
}
