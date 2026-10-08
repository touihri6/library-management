package com.example.author.service.impl;

import com.example.author.dto.AuthorRequest;
import com.example.author.dto.AuthorResponse;
import com.example.author.dto.PageResponse;
import com.example.author.exception.ResourceNotFoundException;
import com.example.author.mapper.AuthorMapper;
import com.example.author.model.entity.Author;
import com.example.author.repository.AuthorRepository;
import com.example.author.service.AuthorService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    public AuthorServiceImpl(AuthorRepository authorRepository, AuthorMapper authorMapper) {
        this.authorRepository = authorRepository;
        this.authorMapper = authorMapper;
    }

    @Override
    public PageResponse<AuthorResponse> findAll(String lastName, String nationality, Pageable pageable) {
        return PageResponse.from(authorRepository.search(lastName, nationality, pageable), authorMapper::toResponse);
    }

    @Override
    public AuthorResponse findById(Long id) {
        return authorMapper.toResponse(getAuthorOrThrow(id));
    }

    @Override
    @Transactional
    public AuthorResponse create(AuthorRequest request) {
        return authorMapper.toResponse(authorRepository.save(authorMapper.toEntity(request)));
    }

    @Override
    @Transactional
    public AuthorResponse update(Long id, AuthorRequest request) {
        Author author = getAuthorOrThrow(id);
        authorMapper.updateEntity(request, author);
        return authorMapper.toResponse(authorRepository.saveAndFlush(author));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        authorRepository.delete(getAuthorOrThrow(id));
    }

    private Author getAuthorOrThrow(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author " + id + " not found"));
    }
}
