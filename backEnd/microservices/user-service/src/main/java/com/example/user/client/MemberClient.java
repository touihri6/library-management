package com.example.user.client;

import com.example.user.dto.MemberCreateRequest;
import com.example.user.dto.MemberSummary;
import com.example.user.dto.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "MEMBER-SERVICE")
public interface MemberClient {

    @GetMapping("/api/v1/members")
    PageResponse<MemberSummary> findByEmail(@RequestParam("email") String email, @RequestParam("size") int size);

    @PostMapping("/api/v1/members")
    MemberSummary create(@RequestBody MemberCreateRequest request);
}
