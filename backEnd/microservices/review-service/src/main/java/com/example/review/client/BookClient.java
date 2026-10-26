package com.example.review.client;

import com.example.review.dto.BookSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "BOOK-SERVICE")
public interface BookClient {

    @GetMapping("/api/v1/books/{id}")
    BookSummary findById(@PathVariable("id") Long id);
}
