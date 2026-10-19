package com.example.book.service.impl;

import com.example.book.client.ReviewClient;
import com.example.book.dto.BookDetails;
import com.example.book.dto.BookRequest;
import com.example.book.dto.BookResponse;
import com.example.book.dto.PageResponse;
import com.example.book.dto.RatingSummary;
import com.example.book.exception.DuplicateResourceException;
import com.example.book.exception.ResourceNotFoundException;
import com.example.book.mapper.BookMapper;
import com.example.book.model.entity.Book;
import com.example.book.model.enums.Genre;
import com.example.book.repository.BookRepository;
import com.example.book.service.BookService;
import feign.FeignException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final ReviewClient reviewClient;

    public BookServiceImpl(BookRepository bookRepository, BookMapper bookMapper, ReviewClient reviewClient) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
        this.reviewClient = reviewClient;
    }

    @Override
    public PageResponse<BookResponse> findAll(String author, Genre genre, Pageable pageable) {
        return PageResponse.from(bookRepository.search(author, genre, pageable), bookMapper::toResponse);
    }

    @Override
    public BookResponse findById(Long id) {
        return bookMapper.toResponse(getBookOrThrow(id));
    }

    @Override
    public BookDetails findDetails(Long id) {
        BookResponse book = bookMapper.toResponse(getBookOrThrow(id));
        return new BookDetails(book, findRatingOrNull(id));
    }

    @Override
    @Transactional
    public BookResponse create(BookRequest request) {
        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new DuplicateResourceException("A book with ISBN " + request.isbn() + " already exists");
        }
        Book saved = bookRepository.save(bookMapper.toEntity(request));
        return bookMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = getBookOrThrow(id);
        if (bookRepository.existsByIsbnAndIdNot(request.isbn(), id)) {
            throw new DuplicateResourceException("A book with ISBN " + request.isbn() + " already exists");
        }
        bookMapper.updateEntity(request, book);

        return bookMapper.toResponse(bookRepository.saveAndFlush(book));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        bookRepository.delete(getBookOrThrow(id));
    }

    private RatingSummary findRatingOrNull(Long id) {
        try {
            return reviewClient.findAverage(id);
        } catch (FeignException ex) {
            return null;
        }
    }

    private Book getBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book " + id + " not found"));
    }
}
