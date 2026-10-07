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

        @Schema(example = "Dune")
        @NotBlank @Size(max = 200)
        String title,

        @Schema(example = "Frank Herbert")
        @NotBlank
        @Size(max = 120)
        String author,

        @Schema(example = "978-0441172719", description = "ISBN-10 or ISBN-13, hyphens allowed")
        @NotBlank
        @Pattern(regexp = "^[0-9][0-9-]{8,15}[0-9X]$", message = "must be a valid ISBN")
        String isbn,

        @Schema(example = "SCIENCE_FICTION")
        @NotNull
        Genre genre,

        @Schema(example = "1965")
        @Min(1450) @Max(2100)
        Integer publicationYear,

        @Schema(example = "9.99")
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2)
        BigDecimal price,

        @Schema(example = "3")
        @NotNull @PositiveOrZero
        Integer availableCopies
) {
}
