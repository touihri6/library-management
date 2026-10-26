package com.example.author.service;

import com.example.author.client.BookClient;
import com.example.author.dto.AuthorBooks;
import com.example.author.dto.AuthorRequest;
import com.example.author.dto.AuthorResponse;
import com.example.author.dto.BookSummary;
import com.example.author.dto.PageResponse;
import com.example.author.exception.InvalidOperationException;
import com.example.author.exception.ResourceNotFoundException;
import com.example.author.mapper.AuthorMapper;
import com.example.author.mapper.AuthorMapperImpl;
import com.example.author.model.entity.Author;
import com.example.author.repository.AuthorRepository;
import com.example.author.service.impl.AuthorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookClient bookClient;

    private final AuthorMapper authorMapper = new AuthorMapperImpl();

    private AuthorService authorService;

    @BeforeEach
    void setUp() {
        authorService = new AuthorServiceImpl(authorRepository, authorMapper, bookClient);
    }

    @Test
    void findBooks_keepsOnlyBooksOfThatExactAuthor() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(herbert()));
        when(bookClient.findByAuthor("Frank Herbert", 100)).thenReturn(page(
                book(1L, "Dune", "Frank Herbert"),
                book(2L, "Dune Messiah", "frank herbert"),
                book(3L, "Other", "Frank Herbertson")));

        AuthorBooks result = authorService.findBooks(1L);

        assertThat(result.author().lastName()).isEqualTo("Herbert");
        assertThat(result.books()).extracting(BookSummary::title).containsExactly("Dune", "Dune Messiah");
    }

    @Test
    void delete_authorWithBooks_throwsInvalidOperation() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(herbert()));
        when(bookClient.findByAuthor("Frank Herbert", 100)).thenReturn(page(book(1L, "Dune", "Frank Herbert")));

        assertThatThrownBy(() -> authorService.delete(1L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessage("Author 1 still has 1 book(s) in the catalog");
        verify(authorRepository, never()).delete(any());
    }

    @Test
    void delete_authorWithoutBooks_deletes() {
        Author author = herbert();
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookClient.findByAuthor("Frank Herbert", 100)).thenReturn(page());

        authorService.delete(1L);

        verify(authorRepository).delete(author);
    }

    private static Author herbert() {
        Author author = new Author();
        author.setId(1L);
        author.setFirstName("Frank");
        author.setLastName("Herbert");
        author.setNationality("American");
        return author;
    }

    private static BookSummary book(Long id, String title, String author) {
        return new BookSummary(id, title, author, "978-0441172719", "SCIENCE_FICTION", 1965, 2, true);
    }

    private static PageResponse<BookSummary> page(BookSummary... books) {
        return new PageResponse<>(List.of(books), 0, 100, books.length, 1, true);
    }

    @Test
    void create_savesAndReturnsResponse() {
        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> {
            Author author = invocation.getArgument(0);
            author.setId(1L);
            return author;
        });

        AuthorResponse response = authorService.create(new AuthorRequest("Isaac", "Asimov", "American", 1920, "Science fiction writer"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.lastName()).isEqualTo("Asimov");
    }

    @Test
    void delete_unknown_throwsNotFound() {
        when(authorRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorService.delete(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
        verify(authorRepository, never()).delete(any());
    }
}
