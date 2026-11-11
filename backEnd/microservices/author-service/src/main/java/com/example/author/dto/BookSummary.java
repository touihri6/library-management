package com.example.author.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A book read from book-service through OpenFeign")
public record BookSummary(
        @Schema(description = "Id of the book in book-service", example = "1")
        Long id,

        @Schema(description = "Title of the book", example = "Dune")
        String title,

        @Schema(description = "Full name of the author", example = "Frank Herbert")
        String author,

        @Schema(description = "ISBN-10 or ISBN-13", example = "978-0441172719")
        String isbn,

        @Schema(description = "Literary genre", example = "SCIENCE_FICTION")
        String genre,

        @Schema(description = "Year of first publication", example = "1965", nullable = true)
        Integer publicationYear,

        @Schema(description = "Copies currently on the shelf", example = "3")
        int availableCopies,

        @Schema(description = "True when at least one copy is available", example = "true")
        boolean available
) {
}
