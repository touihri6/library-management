package com.example.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload used to create or update a review")
public record ReviewRequest(

        @Schema(example = "1")
        @NotNull @Positive
        Long bookId,

        @Schema(example = "Amine")
        @NotBlank @Size(max = 80)
        String reviewerName,

        @Schema(example = "5")
        @NotNull @Min(1) @Max(5)
        Integer rating,

        @Schema(example = "A masterpiece of science fiction")
        @Size(max = 1000)
        String comment
) {
}
