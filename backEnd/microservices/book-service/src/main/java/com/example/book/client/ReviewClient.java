package com.example.book.client;

import com.example.book.dto.RatingSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "REVIEW-SERVICE")
public interface ReviewClient {

    @GetMapping("/api/v1/reviews/books/{bookId}/average")
    RatingSummary findAverage(@PathVariable("bookId") Long bookId);
}
