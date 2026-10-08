package com.example.loan.controller;

import com.example.loan.dto.LoanRequest;
import com.example.loan.dto.LoanResponse;
import com.example.loan.dto.PageResponse;
import com.example.loan.model.enums.LoanStatus;
import com.example.loan.service.LoanService;
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
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/v1/loans")
@Tag(name = "Loans", description = "Book loans: borrow, return and CRUD")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    @Operation(summary = "List loans", description = "Paginated list, optionally filtered by member, book and/or status")
    public PageResponse<LoanResponse> findAll(
            @Parameter(description = "Member id") @RequestParam(required = false) Long memberId,
            @Parameter(description = "Book id") @RequestParam(required = false) Long bookId,
            @Parameter(description = "Loan status") @RequestParam(required = false) LoanStatus status,
            @ParameterObject @PageableDefault(size = 20, sort = "loanDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return loanService.findAll(memberId, bookId, status, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a loan by id")
    @ApiResponse(responseCode = "200", description = "Loan found")
    @ApiResponse(responseCode = "404", description = "Loan not found")
    public LoanResponse findById(@PathVariable Long id) {
        return loanService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Borrow a book (create a loan)")
    @ApiResponse(responseCode = "201", description = "Loan created with status ONGOING")
    @ApiResponse(responseCode = "400", description = "Invalid payload or dueDate not after loanDate")
    public ResponseEntity<LoanResponse> create(@Valid @RequestBody LoanRequest request) {
        LoanResponse created = loanService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a loan (book, member and dates)")
    @ApiResponse(responseCode = "200", description = "Loan updated")
    @ApiResponse(responseCode = "400", description = "Invalid payload or dueDate not after loanDate")
    @ApiResponse(responseCode = "404", description = "Loan not found")
    public LoanResponse update(@PathVariable Long id, @Valid @RequestBody LoanRequest request) {
        return loanService.update(id, request);
    }

    @PatchMapping("/{id}/return")
    @Operation(summary = "Return a borrowed book")
    @ApiResponse(responseCode = "200", description = "Loan returned")
    @ApiResponse(responseCode = "404", description = "Loan not found")
    @ApiResponse(responseCode = "409", description = "Loan already returned")
    public LoanResponse returnLoan(@PathVariable Long id) {
        return loanService.returnLoan(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a loan")
    @ApiResponse(responseCode = "204", description = "Loan deleted")
    @ApiResponse(responseCode = "404", description = "Loan not found")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        loanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
