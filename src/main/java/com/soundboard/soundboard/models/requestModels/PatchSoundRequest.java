package com.soundboard.soundboard.models.requestModels;

import com.soundboard.soundboard.util.SoundCategoryEnum;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PatchSoundRequest(
        @Size(min = 1, message = "Name must not be blank if provided")
        String name,
        
        @Size(max = 250)
        String description,

        SoundCategoryEnum category,

        @Size(max = 25)
        List<String> tags
) {
    public PatchSoundRequest {
        // Preserve null (means "field not provided" for partial update);
        // copy a provided list to prevent external mutation.
        tags = tags == null ? null : List.copyOf(tags);
    }

    @Override
    public List<String> tags() {
        return tags == null ? null : List.copyOf(tags);
    }
}
