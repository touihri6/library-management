package com.example.review.service.impl;

import com.example.review.dto.PageResponse;
import com.example.review.dto.RatingSummary;
import com.example.review.dto.ReviewRequest;
import com.example.review.dto.ReviewResponse;
import com.example.review.exception.ResourceNotFoundException;
import com.example.review.mapper.ReviewMapper;
import com.example.review.model.entity.Review;
import com.example.review.repository.ReviewRepository;
import com.example.review.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    public ReviewServiceImpl(ReviewRepository reviewRepository, ReviewMapper reviewMapper) {
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
    }

    @Override
    public PageResponse<ReviewResponse> findAll(Long bookId, Pageable pageable) {
        Page<Review> page = bookId == null
                ? reviewRepository.findAll(pageable)
                : reviewRepository.findByBookId(bookId, pageable);
        return PageResponse.from(page, reviewMapper::toResponse);
    }

    @Override
    public ReviewResponse findById(Long id) {
        return reviewMapper.toResponse(getReviewOrThrow(id));
    }

    @Override
    @Transactional
    public ReviewResponse create(ReviewRequest request) {
        return reviewMapper.toResponse(reviewRepository.save(reviewMapper.toEntity(request)));
    }

    @Override
    @Transactional
    public ReviewResponse update(Long id, ReviewRequest request) {
        Review review = getReviewOrThrow(id);
        reviewMapper.updateEntity(request, review);
        return reviewMapper.toResponse(reviewRepository.saveAndFlush(review));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        reviewRepository.delete(getReviewOrThrow(id));
    }

    @Override
    public RatingSummary averageForBook(Long bookId) {
        Double average = reviewRepository.averageRating(bookId);
        double rounded = average == null ? 0.0 : Math.round(average * 10) / 10.0;
        return new RatingSummary(bookId, rounded, reviewRepository.countByBookId(bookId));
    }

    private Review getReviewOrThrow(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review " + id + " not found"));
    }
}
