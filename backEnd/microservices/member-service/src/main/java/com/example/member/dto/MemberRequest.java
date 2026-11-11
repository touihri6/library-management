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

        @Schema(description = "First name of the member", example = "Amine")
        @NotBlank @Size(max = 80)
        String firstName,

        @Schema(description = "Last name of the member", example = "Ben Ali")
        @NotBlank @Size(max = 80)
        String lastName,

        @Schema(description = "E-mail address; must be unique", example = "amine@mail.com")
        @NotBlank @Email @Size(max = 120)
        String email,

        @Schema(description = "Phone number", example = "22123456", nullable = true)
        @Size(max = 20)
        String phone,

        @Schema(description = "Membership type", example = "STUDENT")
        @NotNull
        MembershipType membershipType,

        @Schema(description = "Day the member joined the library, today or in the past", example = "2025-09-01")
        @NotNull @PastOrPresent
        LocalDate joinDate,

        @Schema(description = "Keycloak id (sub) of the account linked to this member; only read on creation", example = "5f0c2a9e-3b7d-4f1e-9a51-1c2d3e4f5a6b", nullable = true)
        @Size(max = 36)
        String userId
) {
}
