package com.soundboard.soundboard.models.requestModels;

import com.soundboard.soundboard.util.SoundCategoryEnum;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PatchSoundRequest(
        @Size(min = 1, message = "Name must not be blank if provided")
        String name,

        String description,

        SoundCategoryEnum category,

        List<String> tags
) {}
