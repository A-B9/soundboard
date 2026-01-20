package com.soundboard.soundboard.domain.models.responseModels;

public record GetSoundResponse(
        Long id,
        String name,
        String description,
        String contentType,
        long size
) {
}
