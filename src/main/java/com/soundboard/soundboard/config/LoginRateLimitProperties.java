package com.soundboard.soundboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.rate-limit.login")
public record LoginRateLimitProperties(
        @DefaultValue("10") int capacity,
        @DefaultValue("10") int refillTokens,
        @DefaultValue("60") long refillPeriodSeconds
) {}
