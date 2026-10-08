package com.example.review.controller;

import com.example.review.dto.PageResponse;
import com.example.review.dto.RatingSummary;
import com.example.review.dto.ReviewRequest;
import com.example.review.dto.ReviewResponse;
import com.example.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "Reviews", description = "Ratings and comments on books")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    @Operation(summary = "List reviews", description = "Paginated list, optionally filtered by book")
    public PageResponse<ReviewResponse> findAll(
            @Parameter(description = "Book id") @RequestParam(required = false) Long bookId,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return reviewService.findAll(bookId, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a review by id")
    @ApiResponse(responseCode = "200", description = "Review found")
    @ApiResponse(responseCode = "404", description = "Review not found")
    public ReviewResponse findById(@PathVariable Long id) {
        return reviewService.findById(id);
    }

    @GetMapping("/books/{bookId}/average")
    @Operation(summary = "Average rating of a book", description = "Returns 0.0 and count 0 when the book has no review")
    public RatingSummary averageForBook(@PathVariable Long bookId) {
        return reviewService.averageForBook(bookId);
    }

    @PostMapping
    @Operation(summary = "Create a review")
    @ApiResponse(responseCode = "201", description = "Review created")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    public ResponseEntity<ReviewResponse> create(@Valid @RequestBody ReviewRequest request) {
        ReviewResponse created = reviewService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a review (full replacement)")
    @ApiResponse(responseCode = "200", description = "Review updated")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "404", description = "Review not found")
    public ReviewResponse update(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return reviewService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a review")
    @ApiResponse(responseCode = "204", description = "Review deleted")
    @ApiResponse(responseCode = "404", description = "Review not found")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
