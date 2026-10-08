package com.example.loan.service.impl;

import com.example.loan.dto.LoanRequest;
import com.example.loan.dto.LoanResponse;
import com.example.loan.dto.PageResponse;
import com.example.loan.exception.InvalidDatesException;
import com.example.loan.exception.InvalidOperationException;
import com.example.loan.exception.ResourceNotFoundException;
import com.example.loan.mapper.LoanMapper;
import com.example.loan.model.entity.Loan;
import com.example.loan.model.enums.LoanStatus;
import com.example.loan.repository.LoanRepository;
import com.example.loan.service.LoanService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;

    public LoanServiceImpl(LoanRepository loanRepository, LoanMapper loanMapper) {
        this.loanRepository = loanRepository;
        this.loanMapper = loanMapper;
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
    @Transactional
    public LoanResponse create(LoanRequest request) {
        checkDates(request);
        Loan loan = loanMapper.toEntity(request);
        loan.setStatus(LoanStatus.ONGOING);
        return loanMapper.toResponse(loanRepository.save(loan));
    }

    @Override
    @Transactional
    public LoanResponse update(Long id, LoanRequest request) {
        Loan loan = getLoanOrThrow(id);
        checkDates(request);
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
        return loanMapper.toResponse(loanRepository.saveAndFlush(loan));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        loanRepository.delete(getLoanOrThrow(id));
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
}
