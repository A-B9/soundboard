package com.soundboard.soundboard.models.responseModels.sound;

public record GetSoundResponse(
        Long id,
        String name,
        String description,
        String contentType,
        long size
) implements ResponseBodyModel {
}
