package com.soundboard.soundboard.integration.controller.admin;

import com.soundboard.soundboard.TestJwtHelper;
import com.soundboard.soundboard.integration.BaseIntegrationTest;
import com.soundboard.soundboard.integration.fixtures.SoundSeeder;
import com.soundboard.soundboard.models.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AdminSecurityTests extends BaseIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(AdminSecurityTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SoundSeeder seeder;

    @Autowired
    private TestJwtHelper jwtHelper;

    @BeforeEach
    void setUp() {
        seeder.clearAll();
    }

    @Test
    void adminEndpoint_returns401_whenNoToken() throws Exception {
        log.info("=== TEST: adminEndpoint_returns401_whenNoToken ===");
        log.info("Endpoint : GET /api/soundboard/admin/users");
        log.info("Auth     : No Authorization header");
        log.info("Criteria : HTTP 401 — JwtFilter finds no token, denies request before reaching security role check");

        mockMvc.perform(get("/api/soundboard/admin/users"))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        log.info("PASSED - HTTP 401 returned, unauthenticated request rejected at filter");
    }

    @Test
    void adminEndpoint_returns401_whenMalformedToken() throws Exception {
        log.info("=== TEST: adminEndpoint_returns401_whenMalformedToken ===");
        log.info("Endpoint : GET /api/soundboard/admin/users");
        log.info("Auth     : Authorization: Bearer invalid-token");
        log.info("Criteria : HTTP 401 — JwtFilter catches JwtException, proceeds without authentication context");

        mockMvc.perform(get("/api/soundboard/admin/users")
                        .header("Authorization", "Bearer invalid-token"))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        log.info("PASSED - HTTP 401 returned, malformed JWT correctly rejected by filter");
    }

    @Test
    void adminEndpoint_returns403_whenUserRole() throws Exception {
        log.info("=== TEST: adminEndpoint_returns403_whenUserRole ===");
        log.info("Endpoint : GET /api/soundboard/admin/users");
        log.info("Auth     : Valid Bearer token for USER role");
        log.info("Criteria : HTTP 403 — authenticated but insufficient role, denied by SecurityConfig");

        seeder.seedUser("regular-user");
        String token = jwtHelper.generateTokenForUser("regular-user", Role.USER);

        mockMvc.perform(get("/api/soundboard/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isForbidden());

        log.info("PASSED - HTTP 403 returned, USER role correctly denied access to /api/admin/**");
    }

    @Test
    void adminEndpoint_returns200_whenAdminRole() throws Exception {
        log.info("=== TEST: adminEndpoint_returns200_whenAdminRole ===");
        log.info("Endpoint : GET /api/soundboard/admin/users");
        log.info("Auth     : Valid Bearer token for ADMIN role");
        log.info("Criteria : HTTP 200 — ADMIN role passes security and reaches the handler");

        seeder.seedAdmin("admin-user");
        String token = jwtHelper.generateTokenForUser("admin-user", Role.ADMIN);

        mockMvc.perform(get("/api/soundboard/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk());

        log.info("PASSED - HTTP 200 returned, ADMIN role reached /api/soundboard/admin/users handler");
    }

    @Test
    void adminEndpoint_returns200_whenSuperAdminRole() throws Exception {
        log.info("=== TEST: adminEndpoint_returns200_whenSuperAdminRole ===");
        log.info("Endpoint : GET /api/soundboard/admin/users");
        log.info("Auth     : Valid Bearer token for SUPER_ADMIN role");
        log.info("Criteria : HTTP 200 — SUPER_ADMIN role passes security and reaches the handler");

        seeder.seedSuperAdmin("superadmin-user");
        String token = jwtHelper.generateTokenForUser("superadmin-user", Role.SUPER_ADMIN);

        mockMvc.perform(get("/api/soundboard/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk());

        log.info("PASSED - HTTP 200 returned, SUPER_ADMIN role reached /api/soundboard/admin/users handler");
    }

    @Test
    void soundboardEndpoint_stillAccessible_withUserRole() throws Exception {
        log.info("=== TEST: soundboardEndpoint_stillAccessible_withUserRole ===");
        log.info("Endpoint : GET /api/soundboard/sounds");
        log.info("Auth     : Valid Bearer token for USER role");
        log.info("Criteria : HTTP 200 — admin path gating must not affect existing /api/soundboard/** routes");

        seeder.seedUser("soundboard-user");
        String token = jwtHelper.generateTokenForUser("soundboard-user", Role.USER);

        mockMvc.perform(get("/api/soundboard/sounds")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk());

        log.info("PASSED - HTTP 200 returned, /api/soundboard/sounds remains accessible to USER role");
    }
}
