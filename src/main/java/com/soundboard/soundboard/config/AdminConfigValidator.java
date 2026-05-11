package com.soundboard.soundboard.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AdminConfigValidator {

    private static final Logger log = LoggerFactory.getLogger(AdminConfigValidator.class);

    private final AdminProperties adminProperties;

    public AdminConfigValidator(AdminProperties adminProperties) {
        this.adminProperties = adminProperties;
    }

    @PostConstruct
    void validate() {
        if (!adminProperties.forcePasswordChange()) {
            log.warn("app.admin.force-password-change=false: admin-created and bootstrap accounts will not require a password change on first login. Ensure this is intentional for the current environment.");
        }
    }
}
