package com.soundboard.soundboard.models.requestModels;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(

        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 12, message = "Password must be at least 12 characters long")
        @Pattern(regexp = ".*[^a-zA-Z0-9].*", message = "Password must contain at least one special character")
        String newPassword
) {}
