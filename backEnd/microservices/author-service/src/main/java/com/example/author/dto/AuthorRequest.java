package com.example.author.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload used to create or update an author")
public record AuthorRequest(

        @Schema(example = "Frank")
        @NotBlank @Size(max = 80)
        String firstName,

        @Schema(example = "Herbert")
        @NotBlank @Size(max = 80)
        String lastName,

        @Schema(example = "American")
        @NotBlank @Size(max = 60)
        String nationality,

        @Schema(example = "1920")
        @Min(1000) @Max(2100)
        Integer birthYear,

        @Schema(example = "Author of the Dune saga")
        @Size(max = 2000)
        String biography
) {
}
