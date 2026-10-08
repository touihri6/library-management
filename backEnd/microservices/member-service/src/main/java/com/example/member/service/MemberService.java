package com.example.member.service;

import com.example.member.dto.MemberRequest;
import com.example.member.dto.MemberResponse;
import com.example.member.dto.PageResponse;
import com.example.member.model.enums.MembershipType;
import org.springframework.data.domain.Pageable;

public interface MemberService {

    PageResponse<MemberResponse> findAll(String lastName, MembershipType membershipType, Pageable pageable);

    MemberResponse findById(Long id);

    MemberResponse create(MemberRequest request);

    MemberResponse update(Long id, MemberRequest request);

    void delete(Long id);
}
