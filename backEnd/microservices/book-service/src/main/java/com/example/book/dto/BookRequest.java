package com.example.book.dto;

import com.example.book.model.enums.Genre;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Payload used to create or update a book")
public record BookRequest(

        @Schema(description = "Title of the book", example = "Dune")
        @NotBlank @Size(max = 200)
        String title,

        @Schema(description = "Full name of the author", example = "Frank Herbert")
        @NotBlank
        @Size(max = 120)
        String author,

        @Schema(description = "ISBN-10 or ISBN-13, hyphens allowed; must be unique", example = "978-0441172719")
        @NotBlank
        @Pattern(regexp = "^[0-9][0-9-]{8,15}[0-9X]$", message = "must be a valid ISBN")
        String isbn,

        @Schema(description = "Literary genre", example = "SCIENCE_FICTION")
        @NotNull
        Genre genre,

        @Schema(description = "Year of first publication, between 1450 and 2100", example = "1965", nullable = true)
        @Min(1450) @Max(2100)
        Integer publicationYear,

        @Schema(description = "Price in Tunisian dinars (TND), at most 2 decimals", example = "9.99")
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2)
        BigDecimal price,

        @Schema(description = "Copies on the shelf, 0 or more", example = "3")
        @NotNull @PositiveOrZero
        Integer availableCopies
) {
}
