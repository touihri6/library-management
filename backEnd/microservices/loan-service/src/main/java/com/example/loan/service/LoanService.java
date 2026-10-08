package com.example.loan.service;

import com.example.loan.dto.LoanRequest;
import com.example.loan.dto.LoanResponse;
import com.example.loan.dto.PageResponse;
import com.example.loan.model.enums.LoanStatus;
import org.springframework.data.domain.Pageable;

public interface LoanService {

    PageResponse<LoanResponse> findAll(Long memberId, Long bookId, LoanStatus status, Pageable pageable);

    LoanResponse findById(Long id);

    LoanResponse create(LoanRequest request);

    LoanResponse update(Long id, LoanRequest request);

    LoanResponse returnLoan(Long id);

    void delete(Long id);
}
