package com.example.user.service.impl;

import com.example.user.dto.PageResponse;
import com.example.user.dto.RegisterRequest;
import com.example.user.dto.UserCreateRequest;
import com.example.user.dto.UserResponse;
import com.example.user.exception.DuplicateResourceException;
import com.example.user.exception.ResourceNotFoundException;
import com.example.user.keycloak.KeycloakAdminClient;
import com.example.user.mapper.UserMapper;
import com.example.user.model.entity.UserAccount;
import com.example.user.model.enums.UserRole;
import com.example.user.repository.UserRepository;
import com.example.user.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KeycloakAdminClient keycloakAdminClient;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, KeycloakAdminClient keycloakAdminClient) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.keycloakAdminClient = keycloakAdminClient;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        return create(new UserCreateRequest(request.username(), request.email(), request.firstName(),
                request.lastName(), request.password(), UserRole.MEMBER));
    }

    @Override
    @Transactional
    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("A user with username " + request.username() + " already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("A user with email " + request.email() + " already exists");
        }
        String keycloakId = keycloakAdminClient.createUser(request.username(), request.email(),
                request.firstName(), request.lastName(), request.password());
        try {
            keycloakAdminClient.assignRealmRole(keycloakId, request.role().name());
            UserAccount user = userMapper.toEntity(request);
            user.setKeycloakId(keycloakId);
            return userMapper.toResponse(userRepository.saveAndFlush(user));
        } catch (RuntimeException ex) {
            keycloakAdminClient.deleteUserQuietly(keycloakId);
            throw ex;
        }
    }

    @Override
    @Transactional
    public UserResponse me(Jwt jwt) {
        UserAccount user = userRepository.findByKeycloakId(jwt.getSubject())
                .or(() -> userRepository.findByUsername(jwt.getClaimAsString("preferred_username"))
                        .map(existing -> relink(existing, jwt)))
                .orElseGet(() -> userRepository.save(fromToken(jwt)));
        return userMapper.toResponse(user);
    }

    @Override
    public PageResponse<UserResponse> findAll(UserRole role, Pageable pageable) {
        return PageResponse.from(userRepository.search(role, pageable), userMapper::toResponse);
    }

    @Override
    public UserResponse findById(Long id) {
        return userMapper.toResponse(getUserOrThrow(id));
    }

    @Override
    @Transactional
    public UserResponse changeRole(Long id, UserRole role) {
        UserAccount user = getUserOrThrow(id);
        List<String> otherRoles = Arrays.stream(UserRole.values())
                .filter(other -> other != role)
                .map(Enum::name)
                .toList();
        keycloakAdminClient.removeRealmRoles(user.getKeycloakId(), otherRoles);
        keycloakAdminClient.assignRealmRole(user.getKeycloakId(), role.name());
        user.setRole(role);
        return userMapper.toResponse(userRepository.saveAndFlush(user));
    }

    @Override
    @Transactional
    public UserResponse setEnabled(Long id, boolean enabled) {
        UserAccount user = getUserOrThrow(id);
        keycloakAdminClient.setEnabled(user.getKeycloakId(), enabled);
        user.setEnabled(enabled);
        return userMapper.toResponse(userRepository.saveAndFlush(user));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        UserAccount user = getUserOrThrow(id);
        keycloakAdminClient.deleteUser(user.getKeycloakId());
        userRepository.delete(user);
    }

    private UserAccount fromToken(Jwt jwt) {
        UserAccount user = new UserAccount();
        user.setUsername(jwt.getClaimAsString("preferred_username"));
        copyClaims(jwt, user);
        return user;
    }

    private UserAccount relink(UserAccount user, Jwt jwt) {
        copyClaims(jwt, user);
        return userRepository.saveAndFlush(user);
    }

    private static void copyClaims(Jwt jwt, UserAccount user) {
        user.setKeycloakId(jwt.getSubject());
        user.setEmail(jwt.getClaimAsString("email"));
        user.setFirstName(jwt.getClaimAsString("given_name"));
        user.setLastName(jwt.getClaimAsString("family_name"));
        user.setRole(highestRole(jwt));
        user.setEnabled(true);
    }

    private static UserRole highestRole(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        Collection<?> roles = realmAccess != null && realmAccess.get("roles") instanceof Collection<?> list
                ? list : List.of();
        return Arrays.stream(UserRole.values())
                .filter(role -> roles.contains(role.name()))
                .findFirst()
                .orElse(UserRole.MEMBER);
    }

    private UserAccount getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User " + id + " not found"));
    }
}
