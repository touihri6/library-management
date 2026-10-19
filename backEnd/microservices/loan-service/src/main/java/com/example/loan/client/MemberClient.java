package com.example.loan.client;

import com.example.loan.dto.MemberResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "MEMBER-SERVICE")
public interface MemberClient {

    @GetMapping("/api/v1/members/{id}")
    MemberResponse findById(@PathVariable("id") Long id);
}
