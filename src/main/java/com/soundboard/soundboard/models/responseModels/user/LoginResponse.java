package com.soundboard.soundboard.models.responseModels.user;

import lombok.Builder;

@Builder
public record LoginResponse(
        String username,
        String token,
        String message
) {
}
