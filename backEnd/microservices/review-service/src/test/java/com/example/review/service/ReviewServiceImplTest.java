package com.example.review.service;

import com.example.review.client.BookClient;
import com.example.review.dto.BookSummary;
import com.example.review.dto.RatingSummary;
import com.example.review.dto.ReviewRequest;
import com.example.review.dto.ReviewResponse;
import com.example.review.exception.ResourceNotFoundException;
import com.example.review.mapper.ReviewMapper;
import com.example.review.mapper.ReviewMapperImpl;
import com.example.review.model.entity.Review;
import com.example.review.repository.ReviewRepository;
import com.example.review.service.impl.ReviewServiceImpl;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookClient bookClient;

    private final ReviewMapper reviewMapper = new ReviewMapperImpl();

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewServiceImpl(reviewRepository, reviewMapper, bookClient);
    }

    @Test
    void create_existingBook_savesTheReview() {
        when(bookClient.findById(1L)).thenReturn(new BookSummary(1L, "Dune"));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(10L);
            return review;
        });

        ReviewResponse response = reviewService.create(new ReviewRequest(1L, "Nour", 5, "Great"));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.bookId()).isEqualTo(1L);
    }

    @Test
    void create_unknownBook_throwsNotFoundWithoutSaving() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/v1/books/99", Map.of(), null, StandardCharsets.UTF_8, null);
        when(bookClient.findById(99L)).thenThrow(new FeignException.NotFound("404", request, null, Map.of()));

        assertThatThrownBy(() -> reviewService.create(new ReviewRequest(99L, "Nour", 5, "Great")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Book 99 not found");
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void averageForBook_roundsToOneDecimal() {
        when(reviewRepository.averageRating(1L)).thenReturn(4.6667);
        when(reviewRepository.countByBookId(1L)).thenReturn(3L);

        RatingSummary summary = reviewService.averageForBook(1L);

        assertThat(summary.bookId()).isEqualTo(1L);
        assertThat(summary.average()).isEqualTo(4.7);
        assertThat(summary.count()).isEqualTo(3L);
    }

    @Test
    void average_withoutReviews_returnsZero() {
        when(reviewRepository.averageRating(99L)).thenReturn(null);
        when(reviewRepository.countByBookId(99L)).thenReturn(0L);

        RatingSummary summary = reviewService.averageForBook(99L);

        assertThat(summary.average()).isEqualTo(0.0);
        assertThat(summary.count()).isZero();
    }
}
