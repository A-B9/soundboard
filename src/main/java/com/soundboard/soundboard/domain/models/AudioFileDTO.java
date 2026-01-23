package com.soundboard.soundboard.domain.models;

import jakarta.persistence.Id;

import java.time.Instant;

public record AudioFileDTO(
        String audioName,
        String contentType,
        String mimeType,
        String storedName,
        long size,
        Instant createdAt,
        @Id Long id
        
) {
}
