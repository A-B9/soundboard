package com.soundboard.soundboard.audio;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
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
  
  public Set<String> getAllowedMimeTypes() {
    return Collections.unmodifiableSet(allowedMimeTypes);
  }
  
  private void setAllowedMimeTypes() {
    throw new UnsupportedOperationException("Allowed MIME types are immutable and cannot be modified.");
  }
}
