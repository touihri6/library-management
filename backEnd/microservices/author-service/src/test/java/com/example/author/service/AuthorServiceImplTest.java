package com.example.author.service;

import com.example.author.dto.AuthorRequest;
import com.example.author.dto.AuthorResponse;
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

    private final AuthorMapper authorMapper = new AuthorMapperImpl();

    private AuthorService authorService;

    @BeforeEach
    void setUp() {
        authorService = new AuthorServiceImpl(authorRepository, authorMapper);
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
