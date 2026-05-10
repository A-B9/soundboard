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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DeleteTests extends BaseIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(DeleteTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SoundSeeder seeder;

    @Autowired
    private TestJwtHelper jwtHelper;

    private String ownerToken;
    private String otherToken;
    private SoundEntity seededSound;

    @BeforeAll
    void setUpClass() {
        seeder.clearAll();
        seeder.seedUser("deleteowner");
        seeder.seedUser("deleteother");
        ownerToken = jwtHelper.generateTokenForUser("deleteowner");
        otherToken = jwtHelper.generateTokenForUser("deleteother");
    }

    @BeforeEach
    void setUp() {
        seeder.clearSounds();
        seededSound = seeder.seedSound("DeleteMe", "deleteowner");
        log.info("--- Test Setup --- seeded sound id={}", seededSound.getId());
    }

    @Test
    void deleteSound_returns202_whenOwnerDeletes() throws Exception {
        log.info("=== TEST: deleteSound_returns202_whenOwnerDeletes ===");
        log.info("Endpoint : DELETE /api/soundboard/sounds/{}", seededSound.getId());
        log.info("Auth     : Valid Bearer token for 'deleteowner' (the owner)");
        log.info("Criteria : HTTP 202 Accepted");

        mockMvc.perform(delete("/api/soundboard/sounds/" + seededSound.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andDo(print())
                .andExpect(status().isAccepted());

        log.info("PASSED - HTTP 202 returned, owner successfully deleted their sound");
    }

    @Test
    void deleteSound_returns404_whenNonOwnerDeletes() throws Exception {
        log.info("=== TEST: deleteSound_returns404_whenNonOwnerDeletes ===");
        log.info("Endpoint : DELETE /api/soundboard/sounds/{}", seededSound.getId());
        log.info("Auth     : Valid Bearer token for 'deleteother' (not the owner)");
        log.info("Criteria : HTTP 404 Not Found — sound not found for this user");

        mockMvc.perform(delete("/api/soundboard/sounds/" + seededSound.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andDo(print())
                .andExpect(status().isNotFound());

        log.info("PASSED - HTTP 404 returned, non-owner cannot delete another user's sound");
    }

    @Test
    void deleteSound_returns401_whenNoToken() throws Exception {
        log.info("=== TEST: deleteSound_returns401_whenNoToken ===");
        log.info("Endpoint : DELETE /api/soundboard/sounds/{}", seededSound.getId());
        log.info("Auth     : No Authorization header");
        log.info("Criteria : HTTP 401 Unauthorized");

        mockMvc.perform(delete("/api/soundboard/sounds/" + seededSound.getId()))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        log.info("PASSED - HTTP 401 returned, unauthenticated delete request correctly rejected");
    }

    @Test
    void deleteSound_returns400_whenIdIsNotUuid() throws Exception {
        log.info("=== TEST: deleteSound_returns400_whenIdIsNotUuid ===");
        log.info("Endpoint : DELETE /api/soundboard/sounds/not-a-uuid");
        log.info("Auth     : Valid Bearer token");
        log.info("Criteria : HTTP 400 Bad Request — GlobalExceptionHandler catches MethodArgumentTypeMismatchException");

        mockMvc.perform(delete("/api/soundboard/sounds/not-a-uuid")
                        .header("Authorization", "Bearer " + ownerToken))
                .andDo(print())
                .andExpect(status().isBadRequest());

        log.info("PASSED - HTTP 400 returned, malformed UUID path variable correctly rejected");
    }
}
