package com.soundboard.soundboard.audio;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "app.audio-storage")
public record AudioStorageProperties(
        String basePath,
        Set<String> allowedMimeTypes
) {
    public AudioStorageProperties {
        allowedMimeTypes = allowedMimeTypes == null ? Set.of() : Set.copyOf(allowedMimeTypes);
    }

    @Override
    public Set<String> allowedMimeTypes() {
        return Set.copyOf(allowedMimeTypes);
    }
}
