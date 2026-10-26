package com.example.user.repository;

import com.example.user.model.entity.UserAccount;
import com.example.user.model.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByKeycloakId(String keycloakId);

    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("""
            select u from UserAccount u
            where (:role is null or u.role = :role)
            """)
    Page<UserAccount> search(@Param("role") UserRole role, Pageable pageable);
}
