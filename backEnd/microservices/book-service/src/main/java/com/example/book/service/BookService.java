package com.example.book.service;

import com.example.book.dto.BookRequest;
import com.example.book.dto.BookResponse;
import com.example.book.dto.PageResponse;
import com.example.book.model.enums.Genre;
import org.springframework.data.domain.Pageable;

public interface BookService {

    PageResponse<BookResponse> findAll(String author, Genre genre, Pageable pageable);

    BookResponse findById(Long id);

    BookResponse create(BookRequest request);

    BookResponse update(Long id, BookRequest request);

    void delete(Long id);
}
