package com.example.user.controller;

import com.example.user.dto.PageResponse;
import com.example.user.dto.RegisterRequest;
import com.example.user.dto.RoleUpdateRequest;
import com.example.user.dto.UserCreateRequest;
import com.example.user.dto.UserResponse;
import com.example.user.model.enums.UserRole;
import com.example.user.service.UserService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Library accounts managed in Keycloak")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a MEMBER account (public)")
    @ApiResponse(responseCode = "201", description = "Account created in Keycloak")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "409", description = "Username or email already exists")
    @ApiResponse(responseCode = "502", description = "Keycloak error")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse created = userService.register(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/users/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/me")
    @Operation(summary = "Get the account of the authenticated user")
    @ApiResponse(responseCode = "200", description = "Current user")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return userService.me(jwt);
    }

    @GetMapping
    @Operation(summary = "List users (ADMIN)", description = "Paginated list, optionally filtered by role")
    public PageResponse<UserResponse> findAll(
            @Parameter(description = "Exact role") @RequestParam(required = false) UserRole role,
            @ParameterObject @PageableDefault(size = 20, sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {
        return userService.findAll(role, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by id (ADMIN)")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    public UserResponse findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create a user with any role (ADMIN)")
    @ApiResponse(responseCode = "201", description = "Account created in Keycloak")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "409", description = "Username or email already exists")
    @ApiResponse(responseCode = "502", description = "Keycloak error")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        UserResponse created = userService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Change the realm role of a user (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Role changed")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "502", description = "Keycloak error")
    public UserResponse changeRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return userService.changeRole(id, request.role());
    }

    @PatchMapping("/{id}/enable")
    @Operation(summary = "Enable a user (ADMIN)")
    @ApiResponse(responseCode = "200", description = "User enabled")
    @ApiResponse(responseCode = "404", description = "User not found")
    public UserResponse enable(@PathVariable Long id) {
        return userService.setEnabled(id, true);
    }

    @PatchMapping("/{id}/disable")
    @Operation(summary = "Disable a user, who can no longer log in (ADMIN)")
    @ApiResponse(responseCode = "200", description = "User disabled")
    @ApiResponse(responseCode = "404", description = "User not found")
    public UserResponse disable(@PathVariable Long id) {
        return userService.setEnabled(id, false);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user in Keycloak and locally (ADMIN)")
    @ApiResponse(responseCode = "204", description = "User deleted")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
