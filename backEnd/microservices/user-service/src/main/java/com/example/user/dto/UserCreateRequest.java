package com.example.user.dto;

import com.example.user.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload used by an admin to create an account with any role")
public record UserCreateRequest(

        @Schema(description = "Login, 3 to 50 letters, digits, dots, underscores or hyphens; must be unique", example = "sara")
        @NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "^[a-zA-Z0-9._-]+$")
        String username,

        @Schema(description = "E-mail address; must be unique", example = "sara@library.tn")
        @NotBlank @Email @Size(max = 120)
        String email,

        @Schema(description = "First name", example = "Sara")
        @NotBlank @Size(max = 80)
        String firstName,

        @Schema(description = "Last name", example = "Trabelsi")
        @NotBlank @Size(max = 80)
        String lastName,

        @Schema(description = "Password, 8 to 100 characters, stored only in Keycloak", example = "ChangeMe123!")
        @NotBlank @Size(min = 8, max = 100)
        String password,

        @Schema(description = "Realm role given to the account in Keycloak", example = "LIBRARIAN")
        @NotNull
        UserRole role
) {
}
