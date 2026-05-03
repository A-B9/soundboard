package com.soundboard.soundboard.models;

import java.time.Instant;

public record UserDTO(
        Long id,
        String username,
        String displayName,
        Instant createdAt
) {}
