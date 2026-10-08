package com.example.member.dto;

import com.example.member.model.enums.MembershipType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Payload used to create or update a member")
public record MemberRequest(

        @Schema(example = "Amine")
        @NotBlank @Size(max = 80)
        String firstName,

        @Schema(example = "Ben Ali")
        @NotBlank @Size(max = 80)
        String lastName,

        @Schema(example = "amine@mail.com")
        @NotBlank @Email @Size(max = 120)
        String email,

        @Schema(example = "22123456")
        @Size(max = 20)
        String phone,

        @Schema(example = "STUDENT")
        @NotNull
        MembershipType membershipType,

        @Schema(example = "2025-09-01")
        @NotNull @PastOrPresent
        LocalDate joinDate
) {
}
