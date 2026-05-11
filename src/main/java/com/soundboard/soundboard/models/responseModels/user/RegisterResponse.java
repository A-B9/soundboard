package com.soundboard.soundboard.models.responseModels.user;

import com.soundboard.soundboard.models.Role;
import lombok.Builder;

@Builder
public record RegisterResponse(
        String username,
        String message,
        Role role
) {
}
