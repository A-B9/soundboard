package com.soundboard.soundboard.models.requestModels;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SoundRequestModel(
        @NotBlank(message = "Name for sound is mandatory")
        @NotNull
        @Size(min = 1, max = 30)
        String name,

        @NotBlank(message = "Please provide a description")
        @NotNull
        @Size(min = 1, max = 250)
        String description) implements RequestBodyModel {
}
