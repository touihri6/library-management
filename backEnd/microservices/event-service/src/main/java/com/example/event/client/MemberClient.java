package com.example.event.client;

import com.example.event.dto.MemberContact;
import com.example.event.dto.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "MEMBER-SERVICE")
public interface MemberClient {

    @GetMapping("/api/v1/members")
    PageResponse<MemberContact> findAll(@RequestParam("page") int page, @RequestParam("size") int size);
}
