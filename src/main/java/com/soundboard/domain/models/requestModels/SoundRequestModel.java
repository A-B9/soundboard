package com.soundboard.domain.models.requestModels;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SoundRequestModel(
        @NotBlank(message = "Name for sound is mandatory")
        @NotNull
        String name,

        @NotBlank(message = "Please provide a description")
        @NotNull
        String description) {
}
