package com.example.user.service;

import com.example.user.dto.RegisterRequest;
import com.example.user.dto.UserCreateRequest;
import com.example.user.dto.UserResponse;
import com.example.user.exception.DuplicateResourceException;
import com.example.user.exception.IdentityProviderException;
import com.example.user.keycloak.KeycloakAdminClient;
import com.example.user.mapper.UserMapper;
import com.example.user.mapper.UserMapperImpl;
import com.example.user.model.entity.UserAccount;
import com.example.user.model.enums.UserRole;
import com.example.user.repository.UserRepository;
import com.example.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KeycloakAdminClient keycloakAdminClient;

    private final UserMapper userMapper = new UserMapperImpl();

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, userMapper, keycloakAdminClient);
    }

    @Test
    void register_createsKeycloakUserWithMemberRoleAndSavesProfile() {
        when(keycloakAdminClient.createUser("amine", "amine@mail.com", "Amine", "Ben Ali", "S3cret!pass"))
                .thenReturn("kc-1");
        when(userRepository.saveAndFlush(any(UserAccount.class))).thenAnswer(invocation -> {
            UserAccount user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });

        UserResponse response = userService.register(
                new RegisterRequest("amine", "amine@mail.com", "Amine", "Ben Ali", "S3cret!pass"));

        verify(keycloakAdminClient).assignRealmRole("kc-1", "MEMBER");
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.keycloakId()).isEqualTo("kc-1");
        assertThat(response.role()).isEqualTo(UserRole.MEMBER);
        assertThat(response.enabled()).isTrue();
    }

    @Test
    void create_withExistingUsername_throwsDuplicateWithoutCallingKeycloak() {
        when(userRepository.existsByUsername("sara")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(createRequest(UserRole.LIBRARIAN)))
                .isInstanceOf(DuplicateResourceException.class);
        verifyNoInteractions(keycloakAdminClient);
    }

    @Test
    void create_whenRoleAssignmentFails_rollsBackKeycloakUser() {
        when(keycloakAdminClient.createUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("kc-2");
        doThrow(new IdentityProviderException("Keycloak answered 500", null))
                .when(keycloakAdminClient).assignRealmRole("kc-2", "LIBRARIAN");

        assertThatThrownBy(() -> userService.create(createRequest(UserRole.LIBRARIAN)))
                .isInstanceOf(IdentityProviderException.class);
        verify(keycloakAdminClient).deleteUserQuietly("kc-2");
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void me_unknownSubject_createsProfileFromToken() {
        when(userRepository.findByKeycloakId("kc-9")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("librarian")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.me(librarianToken());

        assertThat(response.username()).isEqualTo("librarian");
        assertThat(response.role()).isEqualTo(UserRole.LIBRARIAN);
        assertThat(response.keycloakId()).isEqualTo("kc-9");
    }

    @Test
    void me_knownUsernameWithNewSubject_relinksExistingProfile() {
        UserAccount existing = account(UserRole.MEMBER);
        existing.setUsername("librarian");
        when(userRepository.findByKeycloakId("kc-9")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("librarian")).thenReturn(Optional.of(existing));
        when(userRepository.saveAndFlush(existing)).thenReturn(existing);

        UserResponse response = userService.me(librarianToken());

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.keycloakId()).isEqualTo("kc-9");
        assertThat(response.role()).isEqualTo(UserRole.LIBRARIAN);
        verify(userRepository, never()).save(any());
    }

    private static Jwt librarianToken() {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("kc-9")
                .claim("preferred_username", "librarian")
                .claim("email", "librarian@library.tn")
                .claim("given_name", "Sara")
                .claim("family_name", "Librarian")
                .claim("realm_access", Map.of("roles", List.of("offline_access", "LIBRARIAN")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }

    @Test
    void changeRole_removesOtherRolesAndAssignsNewOne() {
        UserAccount user = account(UserRole.MEMBER);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);

        UserResponse response = userService.changeRole(3L, UserRole.LIBRARIAN);

        verify(keycloakAdminClient).removeRealmRoles("kc-3", List.of("ADMIN", "MEMBER"));
        verify(keycloakAdminClient).assignRealmRole("kc-3", "LIBRARIAN");
        assertThat(response.role()).isEqualTo(UserRole.LIBRARIAN);
    }

    @Test
    void setEnabled_false_disablesInKeycloakAndLocally() {
        UserAccount user = account(UserRole.MEMBER);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);

        UserResponse response = userService.setEnabled(3L, false);

        verify(keycloakAdminClient).setEnabled("kc-3", false);
        assertThat(response.enabled()).isFalse();
    }

    @Test
    void delete_removesFromKeycloakThenLocally() {
        UserAccount user = account(UserRole.MEMBER);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));

        userService.delete(3L);

        verify(keycloakAdminClient).deleteUser("kc-3");
        verify(userRepository).delete(user);
    }

    private static UserCreateRequest createRequest(UserRole role) {
        return new UserCreateRequest("sara", "sara@library.tn", "Sara", "Trabelsi", "S3cret!pass", role);
    }

    private static UserAccount account(UserRole role) {
        UserAccount user = new UserAccount();
        user.setId(3L);
        user.setKeycloakId("kc-3");
        user.setUsername("member");
        user.setRole(role);
        user.setEnabled(true);
        return user;
    }
}
