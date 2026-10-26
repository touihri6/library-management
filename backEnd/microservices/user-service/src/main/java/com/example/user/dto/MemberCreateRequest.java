package com.example.user.dto;

import java.time.LocalDate;

public record MemberCreateRequest(
        String firstName,
        String lastName,
        String email,
        String phone,
        String membershipType,
        LocalDate joinDate
) {

    public static final String STANDARD = "STANDARD";
}
