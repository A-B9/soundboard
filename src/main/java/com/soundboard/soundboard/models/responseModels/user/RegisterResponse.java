package com.soundboard.soundboard.models.responseModels.user;

import lombok.Builder;

@Builder
public record RegisterResponse(
        String username,
        String message
) {
}
