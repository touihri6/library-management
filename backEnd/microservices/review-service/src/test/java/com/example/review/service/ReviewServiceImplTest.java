package com.example.review.service;

import com.example.review.dto.RatingSummary;
import com.example.review.mapper.ReviewMapper;
import com.example.review.mapper.ReviewMapperImpl;
import com.example.review.repository.ReviewRepository;
import com.example.review.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    private final ReviewMapper reviewMapper = new ReviewMapperImpl();

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewServiceImpl(reviewRepository, reviewMapper);
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
