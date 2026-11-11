package com.example.author.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload used to create or update an author")
public record AuthorRequest(

        @Schema(description = "First name of the author", example = "Frank")
        @NotBlank @Size(max = 80)
        String firstName,

        @Schema(description = "Last name of the author", example = "Herbert")
        @NotBlank @Size(max = 80)
        String lastName,

        @Schema(description = "Nationality of the author", example = "American")
        @NotBlank @Size(max = 60)
        String nationality,

        @Schema(description = "Year of birth, between 1000 and 2100", example = "1920", nullable = true)
        @Min(1000) @Max(2100)
        Integer birthYear,

        @Schema(description = "Short biography, at most 2000 characters", example = "Author of the Dune saga", nullable = true)
        @Size(max = 2000)
        String biography
) {
}
