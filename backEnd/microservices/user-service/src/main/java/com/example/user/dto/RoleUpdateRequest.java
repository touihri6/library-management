package com.example.user.dto;

import com.example.user.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "New realm role of a user")
public record RoleUpdateRequest(

        @Schema(example = "LIBRARIAN")
        @NotNull
        UserRole role
) {
}
