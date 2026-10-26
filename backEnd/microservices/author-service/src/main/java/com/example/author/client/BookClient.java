package com.example.author.client;

import com.example.author.dto.BookSummary;
import com.example.author.dto.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "BOOK-SERVICE")
public interface BookClient {

    @GetMapping("/api/v1/books")
    PageResponse<BookSummary> findByAuthor(@RequestParam("author") String author, @RequestParam("size") int size);
}
