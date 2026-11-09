package com.example.loan.service.impl;

import com.example.loan.client.BookClient;
import com.example.loan.client.MemberClient;
import com.example.loan.dto.BookResponse;
import com.example.loan.dto.LoanDetails;
import com.example.loan.dto.LoanRequest;
import com.example.loan.dto.LoanResponse;
import com.example.loan.dto.MemberResponse;
import com.example.loan.dto.PageResponse;
import com.example.loan.dto.SelfLoanRequest;
import com.example.loan.exception.InvalidDatesException;
import com.example.loan.exception.InvalidOperationException;
import com.example.loan.exception.ResourceNotFoundException;
import com.example.loan.mapper.LoanMapper;
import com.example.loan.messaging.LoanEventPublisher;
import com.example.loan.model.entity.Loan;
import com.example.loan.model.enums.LoanStatus;
import com.example.loan.repository.LoanRepository;
import com.example.loan.service.LoanService;
import feign.FeignException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class LoanServiceImpl implements LoanService {

    static final int SELF_LOAN_DAYS = 14;
    private static final Set<LoanStatus> OPEN_STATUSES = EnumSet.of(LoanStatus.ONGOING, LoanStatus.LATE);

    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;
    private final BookClient bookClient;
    private final MemberClient memberClient;
    private final LoanEventPublisher loanEventPublisher;

    public LoanServiceImpl(LoanRepository loanRepository, LoanMapper loanMapper,
                           BookClient bookClient, MemberClient memberClient,
                           LoanEventPublisher loanEventPublisher) {
        this.loanRepository = loanRepository;
        this.loanMapper = loanMapper;
        this.bookClient = bookClient;
        this.memberClient = memberClient;
        this.loanEventPublisher = loanEventPublisher;
    }

    @Override
    public PageResponse<LoanResponse> findAll(Long memberId, Long bookId, LoanStatus status, Pageable pageable) {
        return PageResponse.from(loanRepository.search(memberId, bookId, status, pageable), loanMapper::toResponse);
    }

    @Override
    public LoanResponse findById(Long id) {
        return loanMapper.toResponse(getLoanOrThrow(id));
    }

    @Override
    public LoanDetails findDetails(Long id) {
        Loan loan = getLoanOrThrow(id);
        BookResponse book = getBookOrThrow(loan.getBookId());
        MemberResponse member = getMemberOrThrow(loan.getMemberId());
        return new LoanDetails(loan.getId(), loan.getLoanDate(), loan.getDueDate(), loan.getReturnDate(),
                loan.getStatus(), book, member);
    }

    @Override
    @Transactional
    public LoanResponse create(LoanRequest request) {
        checkDates(request);
        getMemberOrThrow(request.memberId());
        BookResponse book = getBookOrThrow(request.bookId());
        if (!book.available()) {
            throw new InvalidOperationException("Book " + book.id() + " has no available copy");
        }
        Loan loan = loanMapper.toEntity(request);
        loan.setStatus(LoanStatus.ONGOING);
        Loan saved = loanRepository.save(loan);
        loanEventPublisher.publishLoanCreated(saved);
        return loanMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public LoanResponse borrowForMe(SelfLoanRequest request) {
        MemberResponse member = getMyMemberOrThrow();
        if (loanRepository.existsByMemberIdAndBookIdAndStatusIn(member.id(), request.bookId(), OPEN_STATUSES)) {
            throw new InvalidOperationException("You already have an open loan for book " + request.bookId());
        }
        BookResponse book = getBookOrThrow(request.bookId());
        if (!book.available()) {
            throw new InvalidOperationException("Book " + book.id() + " has no available copy");
        }
        LocalDate today = LocalDate.now();
        Loan loan = new Loan();
        loan.setBookId(book.id());
        loan.setMemberId(member.id());
        loan.setLoanDate(today);
        loan.setDueDate(today.plusDays(SELF_LOAN_DAYS));
        loan.setStatus(LoanStatus.ONGOING);
        Loan saved = loanRepository.save(loan);
        loanEventPublisher.publishLoanCreated(saved);
        return loanMapper.toResponse(saved);
    }

    @Override
    public PageResponse<LoanResponse> findMine(Pageable pageable) {
        MemberResponse member = getMyMemberOrThrow();
        return PageResponse.from(loanRepository.search(member.id(), null, null, pageable), loanMapper::toResponse);
    }

    @Override
    @Transactional
    public LoanResponse update(Long id, LoanRequest request) {
        Loan loan = getLoanOrThrow(id);
        if (!loan.getBookId().equals(request.bookId())) {
            throw new InvalidOperationException("The book of loan " + id + " cannot be changed");
        }
        checkDates(request);
        getMemberOrThrow(request.memberId());
        getBookOrThrow(request.bookId());
        loanMapper.updateEntity(request, loan);
        return loanMapper.toResponse(loanRepository.saveAndFlush(loan));
    }

    @Override
    @Transactional
    public LoanResponse returnLoan(Long id) {
        Loan loan = getLoanOrThrow(id);
        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new InvalidOperationException("Loan " + id + " is already returned");
        }
        loan.setReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);
        Loan saved = loanRepository.saveAndFlush(loan);
        loanEventPublisher.publishLoanReturned(saved);
        return loanMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Loan loan = getLoanOrThrow(id);
        loanRepository.delete(loan);
        if (loan.getStatus() != LoanStatus.RETURNED) {
            loanEventPublisher.publishLoanCancelled(loan);
        }
    }

    private void checkDates(LoanRequest request) {
        if (!request.dueDate().isAfter(request.loanDate())) {
            throw new InvalidDatesException("dueDate must be after loanDate");
        }
    }

    private Loan getLoanOrThrow(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan " + id + " not found"));
    }

    private BookResponse getBookOrThrow(Long id) {
        try {
            return bookClient.findById(id);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Book " + id + " not found");
        }
    }

    private MemberResponse getMyMemberOrThrow() {
        try {
            return memberClient.findMine();
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("No library member is linked to your account");
        }
    }

    private MemberResponse getMemberOrThrow(Long id) {
        try {
            return memberClient.findById(id);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Member " + id + " not found");
        }
    }
}
