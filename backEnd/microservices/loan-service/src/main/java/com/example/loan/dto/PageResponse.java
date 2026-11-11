package com.example.loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Schema(description = "One page of results")
public record PageResponse<T>(
        @Schema(description = "Items of the current page")
        List<T> content,

        @Schema(description = "Index of the current page, starting at 0", example = "0")
        int page,

        @Schema(description = "Requested page size", example = "20")
        int size,

        @Schema(description = "Total number of items across all pages", example = "5")
        long totalElements,

        @Schema(description = "Total number of pages", example = "1")
        int totalPages,

        @Schema(description = "True when this is the last page", example = "true")
        boolean last
) {

    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }
}
