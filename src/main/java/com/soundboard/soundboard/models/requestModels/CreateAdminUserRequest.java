package com.soundboard.soundboard.models.requestModels;

import com.soundboard.soundboard.models.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAdminUserRequest(
        @NotBlank String username,

        @NotBlank
        @Size(min = 12, message = "Password must be at least 12 characters")
        @Pattern(
                regexp = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*",
                message = "Password must contain at least one special character"
        )
        String password,

        @NotNull Role role,

        String displayName
) {}
