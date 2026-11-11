package com.example.author.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "An author with the books of the catalog written by them")
public record AuthorBooks(
        @Schema(description = "The author")
        AuthorResponse author,

        @Schema(description = "Books of the catalog whose author name matches the author's full name, read from book-service")
        List<BookSummary> books
) {
}
