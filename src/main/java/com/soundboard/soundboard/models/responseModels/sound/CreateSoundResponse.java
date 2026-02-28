package com.soundboard.soundboard.models.responseModels.sound;

public record CreateSoundResponse(
        String name,
        String description
) implements ResponseBodyModel {
}
