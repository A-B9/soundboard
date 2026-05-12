package com.soundboard.soundboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.admin")
// Fail-closed: forcePasswordChange defaults to true so insecure behaviour must be explicitly opted into per profile.
public record AdminProperties(@DefaultValue("true") boolean forcePasswordChange) {
}
