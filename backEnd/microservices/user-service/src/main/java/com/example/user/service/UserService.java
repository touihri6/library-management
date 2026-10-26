package com.example.user.service;

import com.example.user.dto.PageResponse;
import com.example.user.dto.RegisterRequest;
import com.example.user.dto.UserCreateRequest;
import com.example.user.dto.UserResponse;
import com.example.user.model.enums.UserRole;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

public interface UserService {

    UserResponse register(RegisterRequest request);

    UserResponse create(UserCreateRequest request);

    UserResponse me(Jwt jwt);

    PageResponse<UserResponse> findAll(UserRole role, Pageable pageable);

    UserResponse findById(Long id);

    UserResponse changeRole(Long id, UserRole role);

    UserResponse setEnabled(Long id, boolean enabled);

    void delete(Long id);
}
