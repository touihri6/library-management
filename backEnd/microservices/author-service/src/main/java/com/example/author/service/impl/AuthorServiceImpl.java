package com.example.author.service.impl;

import com.example.author.client.BookClient;
import com.example.author.dto.AuthorBooks;
import com.example.author.dto.AuthorRequest;
import com.example.author.dto.AuthorResponse;
import com.example.author.dto.BookSummary;
import com.example.author.dto.PageResponse;
import com.example.author.exception.InvalidOperationException;
import com.example.author.exception.ResourceNotFoundException;
import com.example.author.mapper.AuthorMapper;
import com.example.author.model.entity.Author;
import com.example.author.repository.AuthorRepository;
import com.example.author.service.AuthorService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuthorServiceImpl implements AuthorService {

    private static final int MAX_BOOKS = 100;

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
    private final BookClient bookClient;

    public AuthorServiceImpl(AuthorRepository authorRepository, AuthorMapper authorMapper, BookClient bookClient) {
        this.authorRepository = authorRepository;
        this.authorMapper = authorMapper;
        this.bookClient = bookClient;
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
    public AuthorBooks findBooks(Long id) {
        Author author = getAuthorOrThrow(id);
        return new AuthorBooks(authorMapper.toResponse(author), booksOf(author));
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
        Author author = getAuthorOrThrow(id);
        int books = booksOf(author).size();
        if (books > 0) {
            throw new InvalidOperationException("Author " + id + " still has " + books + " book(s) in the catalog");
        }
        authorRepository.delete(author);
    }

    private List<BookSummary> booksOf(Author author) {
        String fullName = author.getFirstName() + " " + author.getLastName();
        return bookClient.findByAuthor(fullName, MAX_BOOKS).content().stream()
                .filter(book -> fullName.equalsIgnoreCase(book.author()))
                .toList();
    }

    private Author getAuthorOrThrow(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author " + id + " not found"));
    }
}
