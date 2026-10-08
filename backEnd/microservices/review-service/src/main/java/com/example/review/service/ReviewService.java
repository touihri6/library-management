package com.example.review.service;

import com.example.review.dto.PageResponse;
import com.example.review.dto.RatingSummary;
import com.example.review.dto.ReviewRequest;
import com.example.review.dto.ReviewResponse;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    PageResponse<ReviewResponse> findAll(Long bookId, Pageable pageable);

    ReviewResponse findById(Long id);

    ReviewResponse create(ReviewRequest request);

    ReviewResponse update(Long id, ReviewRequest request);

    void delete(Long id);

    RatingSummary averageForBook(Long bookId);
}
