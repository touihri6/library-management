package com.example.book.dto;

import com.example.book.model.enums.Genre;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "A book as returned by the API")
public record BookResponse(
        @Schema(description = "Technical id of the book", example = "1")
        Long id,

        @Schema(description = "Title of the book", example = "Dune")
        String title,

        @Schema(description = "Full name of the author", example = "Frank Herbert")
        String author,

        @Schema(description = "ISBN-10 or ISBN-13, unique in the catalogue", example = "978-0441172719")
        String isbn,

        @Schema(description = "Literary genre", example = "SCIENCE_FICTION")
        Genre genre,

        @Schema(description = "Year of first publication", example = "1965", nullable = true)
        Integer publicationYear,

        @Schema(description = "Price in Tunisian dinars (TND)", example = "9.99")
        BigDecimal price,

        @Schema(description = "Copies currently on the shelf; decreases when a loan starts and increases when it ends", example = "3")
        int availableCopies,

        @Schema(description = "Derived field: true when at least one copy is available", example = "true")
        boolean available,

        @Schema(description = "When the book was created (UTC)", example = "2026-10-19T08:30:00Z")
        Instant createdAt,

        @Schema(description = "When the book was last modified (UTC)", example = "2026-10-20T14:05:00Z")
        Instant updatedAt,

        @Schema(description = "Keycloak id (sub) of the user who created the book", example = "5f0c2a9e-3b7d-4f1e-9a51-1c2d3e4f5a6b", nullable = true)
        String createdBy,

        @Schema(description = "Keycloak id (sub) of the user who last modified the book", example = "5f0c2a9e-3b7d-4f1e-9a51-1c2d3e4f5a6b", nullable = true)
        String updatedBy
) {
}
