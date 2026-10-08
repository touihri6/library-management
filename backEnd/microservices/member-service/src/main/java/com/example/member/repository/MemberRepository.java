package com.example.member.repository;

import com.example.member.model.entity.Member;
import com.example.member.model.enums.MembershipType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("""
            select m from Member m
            where (:lastName is null or lower(m.lastName) like lower(concat('%', cast(:lastName as string), '%')))
              and (:membershipType is null or m.membershipType = :membershipType)
            """)
    Page<Member> search(@Param("lastName") String lastName,
                        @Param("membershipType") MembershipType membershipType,
                        Pageable pageable);
}
