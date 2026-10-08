package com.example.member.service.impl;

import com.example.member.dto.MemberRequest;
import com.example.member.dto.MemberResponse;
import com.example.member.dto.PageResponse;
import com.example.member.exception.DuplicateResourceException;
import com.example.member.exception.ResourceNotFoundException;
import com.example.member.mapper.MemberMapper;
import com.example.member.model.entity.Member;
import com.example.member.model.enums.MembershipType;
import com.example.member.repository.MemberRepository;
import com.example.member.service.MemberService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    public MemberServiceImpl(MemberRepository memberRepository, MemberMapper memberMapper) {
        this.memberRepository = memberRepository;
        this.memberMapper = memberMapper;
    }

    @Override
    public PageResponse<MemberResponse> findAll(String lastName, MembershipType membershipType, Pageable pageable) {
        return PageResponse.from(memberRepository.search(lastName, membershipType, pageable), memberMapper::toResponse);
    }

    @Override
    public MemberResponse findById(Long id) {
        return memberMapper.toResponse(getMemberOrThrow(id));
    }

    @Override
    @Transactional
    public MemberResponse create(MemberRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("A member with email " + request.email() + " already exists");
        }
        Member saved = memberRepository.save(memberMapper.toEntity(request));
        return memberMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public MemberResponse update(Long id, MemberRequest request) {
        Member member = getMemberOrThrow(id);
        if (memberRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new DuplicateResourceException("A member with email " + request.email() + " already exists");
        }
        memberMapper.updateEntity(request, member);
        return memberMapper.toResponse(memberRepository.saveAndFlush(member));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        memberRepository.delete(getMemberOrThrow(id));
    }

    private Member getMemberOrThrow(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member " + id + " not found"));
    }
}
