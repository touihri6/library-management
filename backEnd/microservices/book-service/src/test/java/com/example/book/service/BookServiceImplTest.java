package com.example.book.service;

import com.example.book.dto.BookRequest;
import com.example.book.dto.BookResponse;
import com.example.book.exception.DuplicateResourceException;
import com.example.book.exception.ResourceNotFoundException;
import com.example.book.mapper.BookMapper;
import com.example.book.mapper.BookMapperImpl;
import com.example.book.model.entity.Book;
import com.example.book.model.enums.Genre;
import com.example.book.repository.BookRepository;
import com.example.book.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    private final BookMapper bookMapper = new BookMapperImpl();

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookServiceImpl(bookRepository, bookMapper);
    }

    @Test
    void create_savesMappedEntityAndReturnsDto() {
        BookRequest request = request("978-0441172719");
        when(bookRepository.existsByIsbn(request.isbn())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(1L);
            return book;
        });

        BookResponse response = bookService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Dune");
        assertThat(response.available()).isTrue();
    }

    @Test
    void create_rejectsDuplicateIsbn() {
        BookRequest request = request("978-0441172719");
        when(bookRepository.existsByIsbn(request.isbn())).thenReturn(true);

        assertThatThrownBy(() -> bookService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(bookRepository, never()).save(any());
    }

    @Test
    void findById_throwsWhenMissing() {
        when(bookRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findById(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void update_copiesRequestOntoExistingEntity() {
        Book existing = new Book();
        existing.setId(7L);
        existing.setTitle("Old title");
        when(bookRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(bookRepository.existsByIsbnAndIdNot("978-0441172719", 7L)).thenReturn(false);
        when(bookRepository.saveAndFlush(existing)).thenReturn(existing);

        BookResponse response = bookService.update(7L, request("978-0441172719"));

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.title()).isEqualTo("Dune");
        assertThat(existing.getGenre()).isEqualTo(Genre.SCIENCE_FICTION);
    }

    private static BookRequest request(String isbn) {
        return new BookRequest("Dune", "Frank Herbert", isbn, Genre.SCIENCE_FICTION, 1965, new BigDecimal("9.99"), 3);
    }
}
