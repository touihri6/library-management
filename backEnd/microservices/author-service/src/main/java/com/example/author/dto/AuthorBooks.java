package com.example.author.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "An author with the books of the catalog written by them")
public record AuthorBooks(
        AuthorResponse author,
        List<BookSummary> books
) {
}
