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
import com.example.review.security.CurrentUser;
import com.example.review.service.ReviewService;
import feign.FeignException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final BookClient bookClient;
    private final CurrentUser currentUser;

    public ReviewServiceImpl(ReviewRepository reviewRepository, ReviewMapper reviewMapper, BookClient bookClient,
                             CurrentUser currentUser) {
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
        this.bookClient = bookClient;
        this.currentUser = currentUser;
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
        Review review = reviewMapper.toEntity(request);
        review.setUserId(currentUser.id());
        review.setReviewerName(onBehalfName(request).orElseGet(currentUser::displayName));
        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public ReviewResponse update(Long id, ReviewRequest request) {
        Review review = getReviewOrThrow(id);
        checkCanModify(review);
        checkBookExists(request.bookId());
        reviewMapper.updateEntity(request, review);
        onBehalfName(request).ifPresent(review::setReviewerName);
        return reviewMapper.toResponse(reviewRepository.saveAndFlush(review));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Review review = getReviewOrThrow(id);
        checkCanModify(review);
        reviewRepository.delete(review);
    }

    @Override
    public RatingSummary averageForBook(Long bookId) {
        Double average = reviewRepository.averageRating(bookId);
        double rounded = average == null ? 0.0 : Math.round(average * 10) / 10.0;
        return new RatingSummary(bookId, rounded, reviewRepository.countByBookId(bookId));
    }

    private Optional<String> onBehalfName(ReviewRequest request) {
        if (currentUser.isStaff() && StringUtils.hasText(request.reviewerName())) {
            return Optional.of(request.reviewerName().trim());
        }
        return Optional.empty();
    }

    private void checkCanModify(Review review) {
        if (!currentUser.isStaff() && !Objects.equals(review.getUserId(), currentUser.id())) {
            throw new AccessDeniedException("You can only modify your own reviews");
        }
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
