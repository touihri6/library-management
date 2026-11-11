package com.example.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload used to create or update a review")
public record ReviewRequest(

        @Schema(description = "Id of the reviewed book; must exist in book-service", example = "1")
        @NotNull @Positive
        Long bookId,

        @Schema(example = "Amine", description = "Only used when a librarian or admin writes on behalf of someone; members always get the name from their token", nullable = true)
        @Size(max = 80)
        String reviewerName,

        @Schema(description = "Rating from 1 to 5", example = "5")
        @NotNull @Min(1) @Max(5)
        Integer rating,

        @Schema(description = "Free comment, at most 1000 characters", example = "A masterpiece of science fiction", nullable = true)
        @Size(max = 1000)
        String comment
) {
}
