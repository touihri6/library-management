package com.example.loan.service;

import com.example.loan.dto.LoanRequest;
import com.example.loan.dto.LoanResponse;
import com.example.loan.exception.InvalidDatesException;
import com.example.loan.exception.InvalidOperationException;
import com.example.loan.mapper.LoanMapper;
import com.example.loan.mapper.LoanMapperImpl;
import com.example.loan.model.entity.Loan;
import com.example.loan.model.enums.LoanStatus;
import com.example.loan.repository.LoanRepository;
import com.example.loan.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;

    private final LoanMapper loanMapper = new LoanMapperImpl();

    private LoanService loanService;

    @BeforeEach
    void setUp() {
        loanService = new LoanServiceImpl(loanRepository, loanMapper);
    }

    @Test
    void create_setsStatusOngoing() {
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan loan = invocation.getArgument(0);
            loan.setId(1L);
            return loan;
        });

        LoanResponse response = loanService.create(new LoanRequest(1L, 2L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15)));

        assertThat(response.status()).isEqualTo(LoanStatus.ONGOING);
        assertThat(response.returnDate()).isNull();
    }

    @Test
    void create_withDueDateBeforeLoanDate_throwsInvalidDates() {
        LoanRequest request = new LoanRequest(1L, 2L, LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 1));

        assertThatThrownBy(() -> loanService.create(request))
                .isInstanceOf(InvalidDatesException.class);
        verify(loanRepository, never()).save(any());
    }

    @Test
    void returnLoan_alreadyReturned_throwsInvalidOperation() {
        Loan loan = new Loan();
        loan.setId(4L);
        loan.setStatus(LoanStatus.RETURNED);
        when(loanRepository.findById(4L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.returnLoan(4L))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void returnLoan_ongoing_setsReturnedAndToday() {
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setStatus(LoanStatus.ONGOING);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.saveAndFlush(loan)).thenReturn(loan);

        LoanResponse response = loanService.returnLoan(1L);

        assertThat(response.status()).isEqualTo(LoanStatus.RETURNED);
        assertThat(response.returnDate()).isEqualTo(LocalDate.now());
    }
}
