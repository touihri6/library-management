package com.example.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload used by a visitor to create a MEMBER account")
public record RegisterRequest(

        @Schema(example = "amine")
        @NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "^[a-zA-Z0-9._-]+$")
        String username,

        @Schema(example = "amine@mail.com")
        @NotBlank @Email @Size(max = 120)
        String email,

        @Schema(example = "Amine")
        @NotBlank @Size(max = 80)
        String firstName,

        @Schema(example = "Ben Ali")
        @NotBlank @Size(max = 80)
        String lastName,

        @Schema(example = "S3cret!pass")
        @NotBlank @Size(min = 8, max = 100)
        String password
) {
}
