package com.soundboard.soundboard.integration.controller.sound;

import com.soundboard.soundboard.TestJwtHelper;
import com.soundboard.soundboard.integration.BaseIntegrationTest;
import com.soundboard.soundboard.integration.fixtures.SoundSeeder;
import com.soundboard.soundboard.models.SoundEntity;
import com.soundboard.soundboard.util.SoundCategoryEnum;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PatchTests extends BaseIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(PatchTests.class);

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
        seeder.seedUser("otheruser");
        token = jwtHelper.generateTokenForUser("testuser");
    }

    @BeforeEach
    void setUp() {
        log.info("--- Test Setup ---");
        seeder.clearSounds();
        seededSound = seeder.seedSoundWithCategoryAndTags(
                "Explosion",
                "testuser",
                SoundCategoryEnum.BATTLE,
                List.of("loud", "boom")
        );
        log.info("Seeded sound: id={}, name={}, ownedBy={}, category={}, tags={}",
                seededSound.getId(), seededSound.getName(), seededSound.getOwnedBy(),
                seededSound.getCategory(), seededSound.getTags());
    }

    @Test
    void patchSound_returns200_whenNameUpdated() throws Exception {
        log.info("=== TEST: patchSound_returns200_whenNameUpdated ===");
        log.info("Endpoint : PATCH /api/soundboard/sounds/{}", seededSound.getId());
        log.info("Auth     : Valid Bearer token for 'testuser'");
        log.info("Body     : {\"name\":\"Updated Name\"}");
        log.info("Criteria : HTTP 200, name updated, description/category/tags unchanged");

        String body = "{\"name\":\"Updated Name\"}";

        mockMvc.perform(patch("/api/soundboard/sounds/" + seededSound.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(seededSound.getId().toString()))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Test description for Explosion"))
                .andExpect(jsonPath("$.category").value("BATTLE"))
                .andExpect(jsonPath("$.tags", hasSize(2)));

        log.info("PASSED - name updated, other fields unchanged");
    }

    @Test
    void patchSound_returns200_whenDescriptionUpdated() throws Exception {
        log.info("=== TEST: patchSound_returns200_whenDescriptionUpdated ===");
        log.info("Body : {\"description\":\"New description text\"}");
        log.info("Criteria : HTTP 200, description updated");

        String body = "{\"description\":\"New description text\"}";

        mockMvc.perform(patch("/api/soundboard/sounds/" + seededSound.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("New description text"))
                .andExpect(jsonPath("$.name").value("Explosion"));

        log.info("PASSED - description updated");
    }

    @Test
    void patchSound_returns200_whenCategoryUpdated() throws Exception {
        log.info("=== TEST: patchSound_returns200_whenCategoryUpdated ===");
        log.info("Body : {\"category\":\"TAVERN\"}");
        log.info("Criteria : HTTP 200, category updated to TAVERN");

        String body = "{\"category\":\"TAVERN\"}";

        mockMvc.perform(patch("/api/soundboard/sounds/" + seededSound.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("TAVERN"));

        log.info("PASSED - category updated");
    }

    @Test
    void patchSound_returns200_whenTagsUpdated() throws Exception {
        log.info("=== TEST: patchSound_returns200_whenTagsUpdated ===");
        log.info("Body : {\"tags\":[\"epic\",\"intense\"]}");
        log.info("Criteria : HTTP 200, tags replaced");

        String body = "{\"tags\":[\"epic\",\"intense\"]}";

        mockMvc.perform(patch("/api/soundboard/sounds/" + seededSound.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(jsonPath("$.tags[0]").value("epic"))
                .andExpect(jsonPath("$.tags[1]").value("intense"));

        log.info("PASSED - tags updated");
    }

    @Test
    void patchSound_returns200_whenMultipleFieldsUpdated() throws Exception {
        log.info("=== TEST: patchSound_returns200_whenMultipleFieldsUpdated ===");
        log.info("Body : name + category + tags updated together");
        log.info("Criteria : HTTP 200, all three fields reflected in response");

        String body = "{\"name\":\"Epic Battle\",\"category\":\"EPIC\",\"tags\":[\"orchestral\"]}";

        mockMvc.perform(patch("/api/soundboard/sounds/" + seededSound.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Epic Battle"))
                .andExpect(jsonPath("$.category").value("EPIC"))
                .andExpect(jsonPath("$.tags", hasSize(1)))
                .andExpect(jsonPath("$.tags[0]").value("orchestral"));

        log.info("PASSED - multiple fields updated atomically");
    }

    @Test
    void patchSound_returns200_whenTagsClearedWithEmptyList() throws Exception {
        log.info("=== TEST: patchSound_returns200_whenTagsClearedWithEmptyList ===");
        log.info("Body : {\"tags\":[]}");
        log.info("Criteria : HTTP 200, tags cleared to empty list");

        String body = "{\"tags\":[]}";

        mockMvc.perform(patch("/api/soundboard/sounds/" + seededSound.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags", hasSize(0)));

        log.info("PASSED - tags cleared with empty list");
    }

    @Test
    void patchSound_returns400_whenBodyEmpty() throws Exception {
        log.info("=== TEST: patchSound_returns400_whenBodyEmpty ===");
        log.info("Body : {}");
        log.info("Criteria : HTTP 400 - 'At least one field must be provided'");

        String body = "{}";

        mockMvc.perform(patch("/api/soundboard/sounds/" + seededSound.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("At least one field must be provided"));

        log.info("PASSED - empty body rejected with 400");
    }

    @Test
    void patchSound_returns400_whenNameIsBlank() throws Exception {
        log.info("=== TEST: patchSound_returns400_whenNameIsBlank ===");
        log.info("Body : {\"name\":\"\"}");
        log.info("Criteria : HTTP 400 - bean validation @Size(min=1) violation");

        String body = "{\"name\":\"\"}";

        mockMvc.perform(patch("/api/soundboard/sounds/" + seededSound.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists());

        log.info("PASSED - blank name rejected with validation error");
    }

    @Test
    void patchSound_returns400_whenUnknownFieldInBody() throws Exception {
        log.info("=== TEST: patchSound_returns400_whenUnknownFieldInBody ===");
        log.info("Body : {\"unknownField\":\"x\"}");
        log.info("Criteria : HTTP 400 - Jackson rejects unknown property (fail-on-unknown-properties=true)");

        String body = "{\"unknownField\":\"x\"}";

        mockMvc.perform(patch("/api/soundboard/sounds/" + seededSound.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());

        log.info("PASSED - unknown field rejected");
    }

    @Test
    void patchSound_returns400_whenCategoryIsInvalidEnum() throws Exception {
        log.info("=== TEST: patchSound_returns400_whenCategoryIsInvalidEnum ===");
        log.info("Body : {\"category\":\"INVALID\"}");
        log.info("Criteria : HTTP 400 - enum deserialization failure");

        String body = "{\"category\":\"INVALID\"}";

        mockMvc.perform(patch("/api/soundboard/sounds/" + seededSound.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid value 'INVALID' for field 'category'"));

        log.info("PASSED - invalid enum value rejected");
    }

    @Test
    void patchSound_returns404_whenSoundOwnedByAnotherUser() throws Exception {
        log.info("=== TEST: patchSound_returns404_whenSoundOwnedByAnotherUser ===");
        log.info("Criteria : HTTP 404 - ownership enforced via findByIdAndOwnedBy");

        SoundEntity otherSound = seeder.seedSound("OtherSound", "otheruser");
        String body = "{\"name\":\"Hacked\"}";

        mockMvc.perform(patch("/api/soundboard/sounds/" + otherSound.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isNotFound());

        log.info("PASSED - cross-user patch returns 404");
    }

    @Test
    void patchSound_returns404_whenSoundDoesNotExist() throws Exception {
        log.info("=== TEST: patchSound_returns404_whenSoundDoesNotExist ===");
        log.info("Criteria : HTTP 404 - SoundNotFoundException for unknown id");

        UUID nonExistentId = UUID.randomUUID();
        String body = "{\"name\":\"Whatever\"}";

        mockMvc.perform(patch("/api/soundboard/sounds/" + nonExistentId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isNotFound());

        log.info("PASSED - non-existent id returns 404");
    }

    @Test
    void patchSound_returns401_whenNoToken() throws Exception {
        log.info("=== TEST: patchSound_returns401_whenNoToken ===");
        log.info("Criteria : HTTP 401 - request must be rejected before reaching controller");

        String body = "{\"name\":\"Whatever\"}";

        mockMvc.perform(patch("/api/soundboard/sounds/" + seededSound.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        log.info("PASSED - unauthenticated request rejected");
    }

    @Test
    void patchSound_returns400_whenIdIsNotUuid() throws Exception {
        log.info("=== TEST: patchSound_returns400_whenIdIsNotUuid ===");
        log.info("Endpoint : PATCH /api/soundboard/sounds/not-a-uuid");
        log.info("Criteria : HTTP 400 - MethodArgumentTypeMismatchException");

        String body = "{\"name\":\"Whatever\"}";

        mockMvc.perform(patch("/api/soundboard/sounds/not-a-uuid")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());

        log.info("PASSED - malformed UUID path variable rejected");
    }
}
