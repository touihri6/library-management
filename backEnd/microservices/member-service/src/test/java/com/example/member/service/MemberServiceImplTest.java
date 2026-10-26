package com.example.member.service;

import com.example.member.client.LoanClient;
import com.example.member.dto.LoanSummary;
import com.example.member.dto.MemberLoans;
import com.example.member.dto.MemberRequest;
import com.example.member.dto.MemberResponse;
import com.example.member.dto.PageResponse;
import com.example.member.exception.DuplicateResourceException;
import com.example.member.exception.InvalidOperationException;
import com.example.member.exception.ResourceNotFoundException;
import com.example.member.mapper.MemberMapper;
import com.example.member.mapper.MemberMapperImpl;
import com.example.member.model.entity.Member;
import com.example.member.model.enums.MembershipType;
import com.example.member.repository.MemberRepository;
import com.example.member.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private LoanClient loanClient;

    private final MemberMapper memberMapper = new MemberMapperImpl();

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberServiceImpl(memberRepository, memberMapper, loanClient);
    }

    @Test
    void findLoans_returnsMemberWithLoansFromLoanService() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member(1L)));
        when(loanClient.findByMember(1L, 100)).thenReturn(loans(loan(10L, "ONGOING"), loan(11L, "RETURNED")));

        MemberLoans result = memberService.findLoans(1L);

        assertThat(result.member().email()).isEqualTo("amine@mail.com");
        assertThat(result.loans()).extracting(LoanSummary::id).containsExactly(10L, 11L);
    }

    @Test
    void delete_memberWithOngoingLoans_throwsInvalidOperation() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member(1L)));
        when(loanClient.findByMember(1L, 100)).thenReturn(loans(loan(10L, "ONGOING"), loan(11L, "LATE"), loan(12L, "RETURNED")));

        assertThatThrownBy(() -> memberService.delete(1L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessage("Member 1 still has 2 ongoing loan(s)");
        verify(memberRepository, never()).delete(any());
    }

    @Test
    void delete_memberWithOnlyReturnedLoans_deletes() {
        Member member = member(1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(loanClient.findByMember(1L, 100)).thenReturn(loans(loan(12L, "RETURNED")));

        memberService.delete(1L);

        verify(memberRepository).delete(member);
    }

    private static Member member(Long id) {
        Member member = new Member();
        member.setId(id);
        member.setFirstName("Amine");
        member.setLastName("Ben Ali");
        member.setEmail("amine@mail.com");
        member.setMembershipType(MembershipType.STUDENT);
        member.setJoinDate(LocalDate.of(2025, 9, 1));
        return member;
    }

    private static LoanSummary loan(Long id, String status) {
        return new LoanSummary(id, 1L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15), null, status);
    }

    private static PageResponse<LoanSummary> loans(LoanSummary... loans) {
        return new PageResponse<>(List.of(loans), 0, 100, loans.length, 1, true);
    }

    @Test
    void create_withDuplicateEmail_throwsDuplicate() {
        MemberRequest request = request("amine@mail.com");
        when(memberRepository.existsByEmail("amine@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> memberService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void findById_unknown_throwsNotFound() {
        when(memberRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.findById(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void update_keepingOwnEmail_succeeds() {
        Member existing = new Member();
        existing.setId(2L);
        existing.setEmail("sara@mail.com");
        when(memberRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(memberRepository.existsByEmailAndIdNot("sara@mail.com", 2L)).thenReturn(false);
        when(memberRepository.saveAndFlush(existing)).thenReturn(existing);

        MemberResponse response = memberService.update(2L, request("sara@mail.com"));

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.lastName()).isEqualTo("Trabelsi");
    }

    private static MemberRequest request(String email) {
        return new MemberRequest("Sara", "Trabelsi", email, "22111222", MembershipType.STANDARD, LocalDate.of(2025, 1, 10));
    }
}
