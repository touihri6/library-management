package com.example.author.controller;

import com.example.author.dto.AuthorRequest;
import com.example.author.dto.AuthorResponse;
import com.example.author.dto.PageResponse;
import com.example.author.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/authors")
@Tag(name = "Authors", description = "CRUD operations on authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    @Operation(summary = "List authors", description = "Paginated list, optionally filtered by last name and/or nationality")
    public PageResponse<AuthorResponse> findAll(
            @Parameter(description = "Part of the last name (case-insensitive)") @RequestParam(required = false) String lastName,
            @Parameter(description = "Nationality (case-insensitive)") @RequestParam(required = false) String nationality,
            @ParameterObject @PageableDefault(size = 20, sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable) {
        return authorService.findAll(lastName, nationality, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an author by id")
    @ApiResponse(responseCode = "200", description = "Author found")
    @ApiResponse(responseCode = "404", description = "Author not found")
    public AuthorResponse findById(@PathVariable Long id) {
        return authorService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create an author")
    @ApiResponse(responseCode = "201", description = "Author created")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    public ResponseEntity<AuthorResponse> create(@Valid @RequestBody AuthorRequest request) {
        AuthorResponse created = authorService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an author (full replacement)")
    @ApiResponse(responseCode = "200", description = "Author updated")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "404", description = "Author not found")
    public AuthorResponse update(@PathVariable Long id, @Valid @RequestBody AuthorRequest request) {
        return authorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an author")
    @ApiResponse(responseCode = "204", description = "Author deleted")
    @ApiResponse(responseCode = "404", description = "Author not found")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
