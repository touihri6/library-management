package com.example.loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A book read from book-service through OpenFeign")
public record BookResponse(
        @Schema(description = "Technical id of the book", example = "3")
        Long id,

        @Schema(description = "Title of the book", example = "Dune")
        String title,

        @Schema(description = "Full name of the author", example = "Frank Herbert")
        String author,

        @Schema(description = "ISBN-10 or ISBN-13, unique in the catalogue", example = "978-0441172719")
        String isbn,

        @Schema(description = "Copies currently on the shelf", example = "3")
        int availableCopies,

        @Schema(description = "True when at least one copy is available", example = "true")
        boolean available
) {
}
