package com.soundboard.soundboard.integration.controller.sound;

import com.soundboard.soundboard.TestJwtHelper;
import com.soundboard.soundboard.integration.BaseIntegrationTest;
import com.soundboard.soundboard.integration.fixtures.SoundSeeder;
import com.soundboard.soundboard.util.SoundCategoryEnum;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Time;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FilterTests extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SoundSeeder seeder;

    @Autowired
    private TestJwtHelper jwtHelper;

    private String token;
    
    private static final String ENDPOINT = "/api/soundboard/sounds";

    @BeforeAll
    void setUpClass() {
        seeder.clearAll();
        seeder.seedUser("filteruser");
        token = jwtHelper.generateTokenForUser("filteruser");
    }

    @BeforeEach
    void setUp() {
        seeder.clearSounds();
    }

    @Test
    void filterByCategory_returnsMatchingSounds() throws Exception {
        seeder.seedSoundWithCategoryAndTags("Battle Sound", "filteruser", SoundCategoryEnum.BATTLE, List.of());
        seeder.seedSoundWithCategoryAndTags("Tavern Sound", "filteruser", SoundCategoryEnum.TAVERN, List.of());

        mockMvc.perform(get("/api/soundboard/sounds?category=BATTLE")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Battle Sound"))
                .andExpect(jsonPath("$.content[0].category").value("BATTLE"));
    }

    @Test
    void filterByCategory_isCaseInsensitive() throws Exception {
        seeder.seedSoundWithCategoryAndTags("Tavern Sound", "filteruser", SoundCategoryEnum.TAVERN, List.of());

        mockMvc.perform(get("/api/soundboard/sounds?category=tavern")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].category").value("TAVERN"));
    }

    @Test
    void filterByCategory_returnsEmptyPage_whenNoMatch() throws Exception {
        seeder.seedSoundWithCategoryAndTags("Tavern Sound", "filteruser", SoundCategoryEnum.TAVERN, List.of());

        mockMvc.perform(get("/api/soundboard/sounds?category=BATTLE")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void filterByCategory_returns400_whenInvalidValue() throws Exception {
        mockMvc.perform(get("/api/soundboard/sounds?category=INVALID")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void filterByTag_returnsMatchingSounds() throws Exception {
        seeder.seedSoundWithCategoryAndTags("Boss Fight", "filteruser", SoundCategoryEnum.BATTLE, List.of("boss", "fight"));
        seeder.seedSoundWithCategoryAndTags("Calm Walk", "filteruser", SoundCategoryEnum.TRAVEL, List.of("ambient"));

        mockMvc.perform(get("/api/soundboard/sounds?tag=boss")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Boss Fight"));
    }

    @Test
    void filterByTag_isCaseInsensitive() throws Exception {
        seeder.seedSoundWithCategoryAndTags("Boss Fight", "filteruser", SoundCategoryEnum.BATTLE, List.of("boss"));

        mockMvc.perform(get("/api/soundboard/sounds?tag=BOSS")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Boss Fight"));
    }

    @Test
    void filterByTag_returnsEmptyPage_whenNoMatch() throws Exception {
        seeder.seedSoundWithCategoryAndTags("Calm Walk", "filteruser", SoundCategoryEnum.TRAVEL, List.of("ambient"));

        mockMvc.perform(get("/api/soundboard/sounds?tag=boss")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void filterByCategoryAndTag_returnsIntersection() throws Exception {
        seeder.seedSoundWithCategoryAndTags("Boss Fight", "filteruser", SoundCategoryEnum.BATTLE, List.of("boss", "fight"));
        seeder.seedSoundWithCategoryAndTags("Battle Ambient", "filteruser", SoundCategoryEnum.BATTLE, List.of("ambient"));
        seeder.seedSoundWithCategoryAndTags("Tavern Boss", "filteruser", SoundCategoryEnum.TAVERN, List.of("boss"));

        mockMvc.perform(get("/api/soundboard/sounds?category=BATTLE&tag=boss")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Boss Fight"));
    }

    @Test
    void filterByCategoryAndTag_returnsEmptyPage_whenCategoryMatchesButTagDoesNot() throws Exception {
        seeder.seedSoundWithCategoryAndTags("Battle Ambient", "filteruser", SoundCategoryEnum.BATTLE, List.of("ambient"));

        mockMvc.perform(get("/api/soundboard/sounds?category=BATTLE&tag=boss")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }
    
    @Test
    void noFilters_returnsAllSounds() throws Exception {
        seeder.seedSoundWithCategoryAndTags("Sound A", "filteruser", SoundCategoryEnum.BATTLE, List.of("a"));
        seeder.seedSoundWithCategoryAndTags("Sound B", "filteruser", SoundCategoryEnum.TAVERN, List.of("b"));

        mockMvc.perform(get("/api/soundboard/sounds")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }
    
    @Test
    void sortByValidField_CreatedAt() throws Exception {
        seeder.seedSound("sound1", "filteruser");
        seeder.seedSound("sound2", "filteruser");
        
        mockMvc.perform(get(ENDPOINT+"?sortBy=createdAt")
                .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("sound1"))
                .andExpect(jsonPath("$.content[1].name").value("sound2"));
    }
    
    @Test
    void sortByInvalidField_Password() throws Exception {
        mockMvc.perform(get(ENDPOINT+"?sortBy=password")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void sortByValidField_Category() throws Exception {
        seeder.seedSoundWithCategoryAndTags("sound1", "filteruser", SoundCategoryEnum.BATTLE, List.of());
        seeder.seedSoundWithCategoryAndTags("sound2", "filteruser", SoundCategoryEnum.EPIC, List.of());
        
        mockMvc.perform(get(ENDPOINT+"?sortBy=category")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("sound1"))
                .andExpect(jsonPath("$.content[1].name").value("sound2"));
    }
    
    
}
