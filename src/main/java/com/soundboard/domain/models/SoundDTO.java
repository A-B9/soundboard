package com.soundboard.domain.models;

import jakarta.persistence.Id;

import java.time.Instant;

public record SoundDTO(
        @Id Long id,
        String name,
        String description,
        Instant createdAt,
        String storedName,
        long size
) {
}
