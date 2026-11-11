package com.example.loan.controller;

import com.example.loan.dto.LoanDetails;
import com.example.loan.dto.LoanRequest;
import com.example.loan.dto.LoanResponse;
import com.example.loan.dto.PageResponse;
import com.example.loan.dto.SelfLoanRequest;
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
    @Operation(summary = "List loans",
            description = "Paginated list, optionally filtered by member, book and/or status. Requires the LIBRARIAN, ADMIN or SERVICE role (SERVICE is used by the other microservices).")
    @ApiResponse(responseCode = "200", description = "Page of loans")
    @ApiResponse(responseCode = "400", description = "Unknown sort property")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Requires the LIBRARIAN, ADMIN or SERVICE role")
    public PageResponse<LoanResponse> findAll(
            @Parameter(description = "Member id") @RequestParam(required = false) Long memberId,
            @Parameter(description = "Book id") @RequestParam(required = false) Long bookId,
            @Parameter(description = "Loan status") @RequestParam(required = false) LoanStatus status,
            @ParameterObject @PageableDefault(size = 20, sort = "loanDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return loanService.findAll(memberId, bookId, status, pageable);
    }

    @GetMapping("/me")
    @Operation(summary = "List the loans of the signed-in member",
            description = "The member is resolved from the token through member-service. Requires the MEMBER, LIBRARIAN or ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Loans of the signed-in member")
    @ApiResponse(responseCode = "400", description = "Unknown sort property")
    @ApiResponse(responseCode = "404", description = "No member is linked to the account")
    @ApiResponse(responseCode = "503", description = "member-service unavailable")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Requires the MEMBER, LIBRARIAN or ADMIN role")
    public PageResponse<LoanResponse> findMine(
            @ParameterObject @PageableDefault(size = 20, sort = "loanDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return loanService.findMine(pageable);
    }

    @PostMapping("/me")
    @Operation(summary = "Borrow a book for yourself",
            description = "The member comes from the token, the loan starts today and is due in 14 days. Requires the MEMBER, LIBRARIAN or ADMIN role.")
    @ApiResponse(responseCode = "201", description = "Loan created")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "404", description = "Book not found or no member linked to the account")
    @ApiResponse(responseCode = "409", description = "No copy available or the book is already borrowed by you")
    @ApiResponse(responseCode = "503", description = "book-service or member-service unavailable")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Requires the MEMBER, LIBRARIAN or ADMIN role")
    public ResponseEntity<LoanResponse> borrowForMe(@Valid @RequestBody SelfLoanRequest request) {
        LoanResponse created = loanService.borrowForMe(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/loans/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a loan by id", description = "Requires the LIBRARIAN or ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Loan found")
    @ApiResponse(responseCode = "404", description = "Loan not found")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Requires the LIBRARIAN or ADMIN role")
    public LoanResponse findById(@PathVariable Long id) {
        return loanService.findById(id);
    }

    @GetMapping("/{id}/details")
    @Operation(summary = "Get a loan with its book and member",
            description = "Book and member are read from book-service and member-service through OpenFeign. Requires the LIBRARIAN or ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Loan found")
    @ApiResponse(responseCode = "404", description = "Loan, book or member not found")
    @ApiResponse(responseCode = "503", description = "book-service or member-service unavailable")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Requires the LIBRARIAN or ADMIN role")
    public LoanDetails findDetails(@PathVariable Long id) {
        return loanService.findDetails(id);
    }

    @PostMapping
    @Operation(summary = "Borrow a book (create a loan)",
            description = "The book and the member are checked in book-service and member-service through OpenFeign. Requires the LIBRARIAN or ADMIN role.")
    @ApiResponse(responseCode = "201", description = "Loan created with status ONGOING")
    @ApiResponse(responseCode = "400", description = "Invalid payload or dueDate not after loanDate")
    @ApiResponse(responseCode = "404", description = "Book or member not found")
    @ApiResponse(responseCode = "409", description = "Book has no available copy")
    @ApiResponse(responseCode = "503", description = "book-service or member-service unavailable")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Requires the LIBRARIAN or ADMIN role")
    public ResponseEntity<LoanResponse> create(@Valid @RequestBody LoanRequest request) {
        LoanResponse created = loanService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a loan (member and dates, the book cannot be changed)",
            description = "Requires the LIBRARIAN or ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Loan updated")
    @ApiResponse(responseCode = "400", description = "Invalid payload or dueDate not after loanDate")
    @ApiResponse(responseCode = "404", description = "Loan, book or member not found")
    @ApiResponse(responseCode = "409", description = "bookId differs from the loan's book")
    @ApiResponse(responseCode = "503", description = "book-service or member-service unavailable")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Requires the LIBRARIAN or ADMIN role")
    public LoanResponse update(@PathVariable Long id, @Valid @RequestBody LoanRequest request) {
        return loanService.update(id, request);
    }

    @PatchMapping("/{id}/return")
    @Operation(summary = "Return a borrowed book",
            description = "Sets the return date to today and the status to RETURNED. Requires the LIBRARIAN or ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Loan returned")
    @ApiResponse(responseCode = "404", description = "Loan not found")
    @ApiResponse(responseCode = "409", description = "Loan already returned")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Requires the LIBRARIAN or ADMIN role")
    public LoanResponse returnLoan(@PathVariable Long id) {
        return loanService.returnLoan(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a loan", description = "Requires the LIBRARIAN or ADMIN role.")
    @ApiResponse(responseCode = "204", description = "Loan deleted")
    @ApiResponse(responseCode = "404", description = "Loan not found")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Requires the LIBRARIAN or ADMIN role")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        loanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
