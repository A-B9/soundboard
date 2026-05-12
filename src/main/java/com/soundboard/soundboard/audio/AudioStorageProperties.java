package com.soundboard.soundboard.audio;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Set;

@ConfigurationProperties(prefix = "app.audio-storage")
public record AudioStorageProperties(
        String basePath,
        Set<String> allowedMimeTypes
) {
    public Set<String> getAllowedMimeTypes() {
        return Collections.unmodifiableSet(allowedMimeTypes);
    }
}
