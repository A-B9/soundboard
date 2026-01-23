package com.soundboard.soundboard.models.responseModels;

public record CreateSoundResponse(
        String name,
        String description
) implements ResponseBodyModel {
}
