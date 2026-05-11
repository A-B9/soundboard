package com.soundboard.soundboard.models;

import java.time.Instant;
import java.util.UUID;

public record UserDTO(
        UUID id,
        String username,
        String displayName,
        Instant createdAt,
        Role role
) {}
