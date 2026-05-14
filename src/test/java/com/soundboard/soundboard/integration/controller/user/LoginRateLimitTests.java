package com.soundboard.soundboard.integration.controller.user;

import com.soundboard.soundboard.integration.BaseIntegrationTest;
import com.soundboard.soundboard.integration.fixtures.SoundSeeder;
import com.soundboard.soundboard.security.filter.LoginRateLimitFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {
        "app.rate-limit.login.capacity=2",
        "app.rate-limit.login.refill-tokens=2",
        "app.rate-limit.login.refill-period-seconds=60"
})
class LoginRateLimitTests extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private SoundSeeder seeder;
    @Autowired private LoginRateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() {
        seeder.clearAll();
        rateLimitFilter.clearBuckets();
    }

    @Test
    void login_returns200_withinCapacity() throws Exception {
        seeder.seedUserWithPassword("alice", "OldPassword1!");

        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/soundboard/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username":"alice","password":"OldPassword1!"}
                                    """))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void login_returns429_whenCapacityExceeded() throws Exception {
        seeder.seedUserWithPassword("bob", "OldPassword1!");

        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/soundboard/user/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"username":"bob","password":"OldPassword1!"}
                            """));
        }

        mockMvc.perform(post("/api/soundboard/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"bob","password":"OldPassword1!"}
                                """))
                .andExpect(status().is(429))
                .andExpect(jsonPath("$.error").value("Too many login attempts. Please try again later."));
    }

    @Test
    void rateLimiting_doesNotApply_toOtherEndpoints() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/soundboard/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username":"user%d","password":"ValidPassword1!"}
                                    """.formatted(i)))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void rateLimiting_doesNotApply_toAuthenticatedEndpoints() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/soundboard/sounds"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void clearBuckets_resetsLimit_allowingNewRequests() throws Exception {
        seeder.seedUserWithPassword("carol", "OldPassword1!");

        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/soundboard/user/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"username":"carol","password":"OldPassword1!"}
                            """));
        }

        rateLimitFilter.clearBuckets();

        mockMvc.perform(post("/api/soundboard/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"carol","password":"OldPassword1!"}
                                """))
                .andExpect(status().isOk());
    }
}
