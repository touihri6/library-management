package com.example.member.client;

import com.example.member.dto.LoanSummary;
import com.example.member.dto.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "LOAN-SERVICE")
public interface LoanClient {

    @GetMapping("/api/v1/loans")
    PageResponse<LoanSummary> findByMember(@RequestParam("memberId") Long memberId, @RequestParam("size") int size);
}
