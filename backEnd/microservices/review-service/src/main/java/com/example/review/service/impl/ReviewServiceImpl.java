package com.example.review.service.impl;

import com.example.review.client.BookClient;
import com.example.review.dto.PageResponse;
import com.example.review.dto.RatingSummary;
import com.example.review.dto.ReviewRequest;
import com.example.review.dto.ReviewResponse;
import com.example.review.exception.ResourceNotFoundException;
import com.example.review.mapper.ReviewMapper;
import com.example.review.model.entity.Review;
import com.example.review.repository.ReviewRepository;
import com.example.review.service.ReviewService;
import feign.FeignException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final BookClient bookClient;

    public ReviewServiceImpl(ReviewRepository reviewRepository, ReviewMapper reviewMapper, BookClient bookClient) {
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
        this.bookClient = bookClient;
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
        checkBookExists(request.bookId());
        return reviewMapper.toResponse(reviewRepository.save(reviewMapper.toEntity(request)));
    }

    @Override
    @Transactional
    public ReviewResponse update(Long id, ReviewRequest request) {
        Review review = getReviewOrThrow(id);
        checkBookExists(request.bookId());
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

    private void checkBookExists(Long bookId) {
        try {
            bookClient.findById(bookId);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Book " + bookId + " not found");
        }
    }

    private Review getReviewOrThrow(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review " + id + " not found"));
    }
}
