package com.soundboard.soundboard.models.responseModels.sound;

import com.soundboard.soundboard.util.SoundCategoryEnum;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GetSoundResponse(
        UUID id,
        String name,
        String description,
        String ownedBy,
        SoundCategoryEnum category,
        List<String> tags,
        Instant createdAt,
        Instant recentUpdate
) implements ResponseBodyModel {
    
    public GetSoundResponse {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    @Override
    public List<String> tags() {
        return List.copyOf(tags);
    }
}
