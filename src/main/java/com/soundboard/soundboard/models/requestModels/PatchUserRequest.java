package com.soundboard.soundboard.models.requestModels;

import jakarta.validation.constraints.NotNull;

public record PatchUserRequest(
        @NotNull
        Boolean mustChangePassword
) {
}
