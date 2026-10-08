package com.example.author.service;

import com.example.author.dto.AuthorRequest;
import com.example.author.dto.AuthorResponse;
import com.example.author.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface AuthorService {

    PageResponse<AuthorResponse> findAll(String lastName, String nationality, Pageable pageable);

    AuthorResponse findById(Long id);

    AuthorResponse create(AuthorRequest request);

    AuthorResponse update(Long id, AuthorRequest request);

    void delete(Long id);
}
