package com.soundboard.soundboard.domain;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

@ConfigurationProperties(prefix = "app.audio-storage")
@Component
public record AudioStorageProperties(
        String basePath,
        Set<String> allowedMimeTypes
) {
  public AudioStorageProperties() {
    this(
            "./SoundAudio",
            Set.of(
                    "audio/mp3",
                    "audio/wav",
                    "audio/wave"
            )
    );
  }
}
