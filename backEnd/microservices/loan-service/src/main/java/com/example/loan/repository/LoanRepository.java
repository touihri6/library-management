package com.example.loan.repository;

import com.example.loan.model.entity.Loan;
import com.example.loan.model.enums.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query("""
            select l from Loan l
            where (:memberId is null or l.memberId = :memberId)
              and (:bookId is null or l.bookId = :bookId)
              and (:status is null or l.status = :status)
            """)
    Page<Loan> search(@Param("memberId") Long memberId,
                      @Param("bookId") Long bookId,
                      @Param("status") LoanStatus status,
                      Pageable pageable);
}
