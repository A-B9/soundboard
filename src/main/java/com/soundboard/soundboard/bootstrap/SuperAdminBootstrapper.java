package com.soundboard.soundboard.bootstrap;

import com.soundboard.soundboard.config.AdminProperties;
import com.soundboard.soundboard.config.BootstrapProperties;
import com.soundboard.soundboard.models.Role;
import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.repository.MyUserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;

@Component
public class SuperAdminBootstrapper implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminBootstrapper.class);

    private final BootstrapProperties bootstrapProperties;
    private final AdminProperties adminProperties;
    private final MyUserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    public SuperAdminBootstrapper(BootstrapProperties bootstrapProperties,
                                  AdminProperties adminProperties,
                                  MyUserRepo userRepo,
                                  PasswordEncoder passwordEncoder,
                                  Environment env) {
        this.bootstrapProperties = bootstrapProperties;
        this.adminProperties = adminProperties;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.env = env;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("prod");

        String username = bootstrapProperties.username();
        String password = bootstrapProperties.password();

        boolean missingUsername = username == null || username.isBlank();
        boolean missingPassword = password == null || password.isBlank();

        if (isProd && (missingUsername || missingPassword)) {
            throw new IllegalStateException(
                    "APP_BOOTSTRAP_USERNAME and APP_BOOTSTRAP_PASSWORD must be set in the prod environment");
        }

        if (missingUsername || missingPassword) {
            log.info("Bootstrap credentials not configured — skipping SUPER_ADMIN seeding");
            return;
        }

        if (userRepo.existsByRole(Role.SUPER_ADMIN)) {
            log.info("SUPER_ADMIN account already exists — skipping bootstrap");
            return;
        }

        boolean mustChange = adminProperties.forcePasswordChange();
        Users superAdmin = Users.builder()
                .username(username)
                .displayName(username)
                .password(passwordEncoder.encode(password))
                .createdAt(Instant.now())
                .active(true)
                .role(Role.SUPER_ADMIN)
                .mustChangePassword(mustChange)
                .build();
        userRepo.save(superAdmin);

        log.warn("AUDIT: Bootstrap SUPER_ADMIN '{}' created. mustChangePassword={}", username, mustChange);
    }
}
