package com.example.member.controller;

import com.example.member.dto.MemberRequest;
import com.example.member.dto.MemberResponse;
import com.example.member.dto.PageResponse;
import com.example.member.model.enums.MembershipType;
import com.example.member.service.MemberService;
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
@RequestMapping("/api/v1/members")
@Tag(name = "Members", description = "CRUD operations on library members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    @Operation(summary = "List members", description = "Paginated list, optionally filtered by last name and/or membership type")
    public PageResponse<MemberResponse> findAll(
            @Parameter(description = "Part of the last name (case-insensitive)") @RequestParam(required = false) String lastName,
            @Parameter(description = "Exact membership type") @RequestParam(required = false) MembershipType membershipType,
            @ParameterObject @PageableDefault(size = 20, sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable) {
        return memberService.findAll(lastName, membershipType, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a member by id")
    @ApiResponse(responseCode = "200", description = "Member found")
    @ApiResponse(responseCode = "404", description = "Member not found")
    public MemberResponse findById(@PathVariable Long id) {
        return memberService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create a member")
    @ApiResponse(responseCode = "201", description = "Member created")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "409", description = "Email already exists")
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberRequest request) {
        MemberResponse created = memberService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a member (full replacement)")
    @ApiResponse(responseCode = "200", description = "Member updated")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "404", description = "Member not found")
    @ApiResponse(responseCode = "409", description = "Email already used by another member")
    public MemberResponse update(@PathVariable Long id, @Valid @RequestBody MemberRequest request) {
        return memberService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a member")
    @ApiResponse(responseCode = "204", description = "Member deleted")
    @ApiResponse(responseCode = "404", description = "Member not found")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
