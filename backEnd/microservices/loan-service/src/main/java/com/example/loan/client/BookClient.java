package com.example.loan.client;

import com.example.loan.dto.BookResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "BOOK-SERVICE")
public interface BookClient {

    @GetMapping("/api/v1/books/{id}")
    BookResponse findById(@PathVariable("id") Long id);
}
