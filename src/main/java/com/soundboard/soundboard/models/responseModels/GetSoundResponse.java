package com.soundboard.soundboard.models.responseModels;

public record GetSoundResponse(
        Long id,
        String name,
        String description,
        String contentType,
        long size
) implements ResponseBodyModel {
}
