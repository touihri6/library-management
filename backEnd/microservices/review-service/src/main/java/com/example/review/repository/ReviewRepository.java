package com.example.review.repository;

import com.example.review.model.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByBookId(Long bookId, Pageable pageable);

    long countByBookId(Long bookId);

    @Query("select avg(r.rating) from Review r where r.bookId = :bookId")
    Double averageRating(@Param("bookId") Long bookId);
}
