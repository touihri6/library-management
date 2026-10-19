package com.example.loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A book read from book-service through OpenFeign")
public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        int availableCopies,
        boolean available
) {
}
