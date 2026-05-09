package com.soundboard.soundboard.integration.controller.sound;

import com.soundboard.soundboard.TestJwtHelper;
import com.soundboard.soundboard.integration.BaseIntegrationTest;
import com.soundboard.soundboard.integration.fixtures.SoundSeeder;
import com.soundboard.soundboard.models.SoundEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetTests extends BaseIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(GetTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SoundSeeder seeder;

    @Autowired
    private TestJwtHelper jwtHelper;

    private String token;
    private SoundEntity seededSound;

    @BeforeAll
    void setUpClass() {
        seeder.clearAll();
        seeder.seedUser("testuser");
        token = jwtHelper.generateTokenForUser("testuser");
    }

    @BeforeEach
    void setUp() {
        log.info("--- Test Setup ---");
        seeder.clearSounds();
        seededSound = seeder.seedSound("Explosion", "testuser");
        log.info("Seeded user: testuser");
        log.info("Seeded sound: id={}, name={}, ownedBy={}", seededSound.getId(), seededSound.getName(), seededSound.getOwnedBy());
        log.info("Generated JWT for: testuser");
    }

    @Test
    void getAllSounds_returnsPage_whenSoundsExist() throws Exception {
        log.info("=== TEST: getAllSounds_returnsPage_whenSoundsExist ===");
        log.info("Endpoint : GET /api/soundboard/sounds");
        log.info("Auth     : Valid Bearer token for 'testuser'");
        log.info("Criteria : HTTP 200, response body contains a paginated list, first item name = 'Explosion'");

        ResultActions result = mockMvc.perform(get("/api/soundboard/sounds")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Explosion"));

        log.info("PASSED - HTTP 200 returned, page content contains seeded sound 'Explosion'");
    }

    @Test
    void getAllSounds_returns401_whenNoToken() throws Exception {
        log.info("=== TEST: getAllSounds_returns401_whenNoToken ===");
        log.info("Endpoint : GET /api/soundboard/sounds");
        log.info("Auth     : No Authorization header");
        log.info("Criteria : HTTP 401 Unauthorized — request must be rejected before reaching the controller");

        mockMvc.perform(get("/api/soundboard/sounds"))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        log.info("PASSED - HTTP 401 returned, unauthenticated request correctly rejected");
    }

    @Test
    void getSound_returnsSound_whenExists() throws Exception {
        log.info("=== TEST: getSound_returnsSound_whenExists ===");
        log.info("Endpoint : GET /api/soundboard/sounds/{}", seededSound.getId());
        log.info("Auth     : Valid Bearer token for 'testuser'");
        log.info("Criteria : HTTP 200, response body contains id={} and name='Explosion'", seededSound.getId());

        mockMvc.perform(get("/api/soundboard/sounds/" + seededSound.getId())
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(seededSound.getId()))
                .andExpect(jsonPath("$.name").value("Explosion"));

        log.info("PASSED - HTTP 200 returned, sound id={} name='Explosion' correctly retrieved", seededSound.getId());
    }
}
