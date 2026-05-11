package com.soundboard.soundboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "app.admin")
@Component
public record AdminProperties(boolean forcePasswordChange) {
    // Defaults to true (fail-closed): insecure behaviour must be explicitly opted into per profile.
    public AdminProperties() {
        this(true);
    }
}
