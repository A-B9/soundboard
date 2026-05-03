package com.soundboard.soundboard.models;

import com.soundboard.soundboard.util.SoundCategoryEnum;
import java.time.Instant;
import java.util.List;

public record SoundDTO(
        Long id,
        String name,
        String description,
        String ownedBy,
        SoundCategoryEnum category,
        List<String> tags,
        Instant createdAt,
        Instant recentUpdate
) {}
