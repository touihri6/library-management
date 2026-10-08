package com.example.author.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "An author as returned by the API")
public record AuthorResponse(
        Long id,
        String firstName,
        String lastName,
        String nationality,
        Integer birthYear,
        String biography,
        Instant createdAt,
        Instant updatedAt
) {
}
