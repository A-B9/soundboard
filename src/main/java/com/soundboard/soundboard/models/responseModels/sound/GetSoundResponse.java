package com.soundboard.soundboard.models.responseModels.sound;

import com.soundboard.soundboard.util.SoundCategoryEnum;
import java.time.Instant;
import java.util.List;

public record GetSoundResponse(
        Long id,
        String name,
        String description,
        String ownedBy,
        SoundCategoryEnum category,
        List<String> tags,
        Instant createdAt,
        Instant recentUpdate
) implements ResponseBodyModel {}
